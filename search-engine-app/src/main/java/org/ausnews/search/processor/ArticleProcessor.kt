package org.ausnews.search.processor

import com.google.inject.Inject
import com.yahoo.docproc.DocumentProcessor
import com.yahoo.docproc.Processing
import com.yahoo.document.*
import com.yahoo.document.datatypes.DoubleFieldValue
import com.yahoo.language.Linguistics
import com.yahoo.search.searchchain.ExecutionFactory
import com.yahoo.document.datatypes.LongFieldValue
import com.yahoo.document.datatypes.StringFieldValue
import com.yahoo.document.update.FieldUpdate
import com.yahoo.documentapi.*
import com.yahoo.search.Query
import com.yahoo.search.result.Hit
import kotlinx.coroutines.*
import java.lang.Exception
import java.util.logging.Logger

class ArticleProcessor @Inject constructor(
    acc: DocumentAccess,
    linguistics: Linguistics,
    executionFactory: ExecutionFactory
) : DocumentProcessor() {
    private val asyncSession: AsyncSession = acc.createAsyncSession(AsyncParameters())
    private val executionFactory: ExecutionFactory = executionFactory
    private val linguistics: Linguistics = linguistics
    private val documentAccess: DocumentAccess = acc
    private val REQUIRED_RELEVANCE_SCORE = 0.11

    override fun process(processing: Processing): Progress {
        try {
            for (op in processing.documentOperations) {
                if (op is DocumentPut) {
                    val document = op.document
                    return getProgress(op, document.id)
                } else if (op is DocumentUpdate) {
                    /* Almost everything comes through as an update, as we use create on updates that don't exist.
                     * If an update has our group field name, it's an update triggered from below - be done
                     * If an udpate has a twitter link, it's just the augmenter, no need to update groups.
                     */
                    if (op.getFieldUpdate(GROUP_FIELD_NAME) != null ||
                            op.getFieldUpdate(TWITTER_FIELD_NAME) != null) {
                        return Progress.DONE;
                    }
                    return getProgress(op, op.id)
                }
            }
        } catch (e: Exception) {
            logger.severe("Exception processing document: $e")
            e.printStackTrace()
        }
        return Progress.DONE
    }

    override fun deconstruct() {
        super.deconstruct()
        asyncSession.destroy()
    }

    private fun getProgress(operation: DocumentOperation, id: DocumentId): Progress {
        val deferred = deferredActions[id.toString()]
        deferred?.let {
            return when (deferred.isCompleted) {
                true -> {
                    deferredActions.remove(id.toString())
                    Progress.DONE
                }
                false -> Progress.LATER
            }
        }
        deferredActions[id.toString()] = GlobalScope.async {
            addTopicToRelatedArticles(operation)
        }
        return Progress.LATER
    }

    private suspend fun addTopicToRelatedArticles(operation: DocumentOperation) {
        try {
            val query = Query()
            val headline = when (operation) {
                is DocumentPut -> operation.document.getFieldValue("headline").toString()
                is DocumentUpdate -> operation.getFieldUpdate("headline").getValueUpdate(0).value.toString()
                else -> ""
            }
            val summary = when (operation) {
                is DocumentPut -> operation.document.getFieldValue("abstract").toString()
                is DocumentUpdate -> operation.getFieldUpdate("abstract").getValueUpdate(0).value.toString()
                else -> ""
            }
            val time = when (operation) {
                is DocumentPut -> (operation.document.getFieldValue("firstpubtime") as LongFieldValue).long - SAME_STORY_TIMELIMIT
                is DocumentUpdate -> operation.getFieldUpdate("firstpubtime").getValueUpdate(0).value.toString()
                    .toLong() - SAME_STORY_TIMELIMIT
                else -> 0
            }
            query.properties()["headline"] = headline
            query.properties()["abstract"] = summary
            query.properties()["hits"] = 30
            query.properties()["query"] = "firstpubtime:>$time"
            query.properties()["searchChain"] = "related"
            logger.info("Searching $headline")
            val execution = executionFactory.newExecution("related")
            val result = execution.search(query)
            val documentId = when (operation) {
                is DocumentPut -> operation.document.id.toString()
                is DocumentUpdate -> operation.id.toString()
                else -> throw Exception("Unknown operation")
            }
            execution.fill(result)
            val hits = result.hits().filter {
                it.getField("documentid").toString() != documentId &&
                        it.relevance.score > REQUIRED_RELEVANCE_SCORE
            }
            logger.info("Got hits: " + hits.size)
            if (hits.isEmpty()) return
            var topic: String? = null
            var relevance: Double = 0.0
            for (hit in hits) {
                if (hit.isMeta) continue
                if (hit.getField(GROUP_FIELD_NAME) != null) {
                    topic = hit.getField(GROUP_FIELD_NAME).toString()
                    relevance = hit.relevance.score
                    break
                }
            }
            val t = topic ?: documentId
            when (operation) {
                is DocumentPut -> {
                    val doc = operation.document
                    doc.setFieldValue(doc.dataType.getField(GROUP_FIELD_NAME), StringFieldValue(t))
                }
                is DocumentUpdate -> {
                    val fieldUpdate =
                        FieldUpdate.createAssign(operation.documentType.getField(GROUP_FIELD_NAME), StringFieldValue(t))
                    val fieldRelevanceUpdate = FieldUpdate.createAssign(operation.documentType.getField(GROUP_FIELD_RELEVANCE_NAME), DoubleFieldValue(relevance))
                    operation.addFieldUpdate(fieldUpdate)
                    operation.addFieldUpdate(fieldRelevanceUpdate)
                }
            }
            val type = when (operation) {
                is DocumentPut -> operation.document.dataType
                is DocumentUpdate -> operation.documentType
                else -> throw Exception("Couldn't determine type")
            }
            hits.forEach { hit: Hit ->
                val docId = hit.getField("documentid").toString()
                val update = DocumentUpdate(type, docId)
                val fieldValue = StringFieldValue(t)
                update.addFieldUpdate(FieldUpdate.createAssign(type.getField(GROUP_FIELD_RELEVANCE_NAME), DoubleFieldValue(hit.relevance.score)))
                update.addFieldUpdate(FieldUpdate.createAssign(type.getField(GROUP_FIELD_NAME), fieldValue))
                val relevance = hit.relevance.score
                logger.info("Set group_doc_id to $fieldValue for $docId. Relevance: $relevance")
                asyncSession.update(update)
            }
        } catch (e: Exception) {
            logger.severe(e.toString())
        }
    }

    companion object {
        private val logger = Logger.getLogger(ArticleProcessor::class.java.name)
        private const val GROUP_FIELD_NAME = "group_doc_id"
        private const val GROUP_FIELD_RELEVANCE_NAME = "group_doc_id_relevance"
        private const val TWITTER_FIELD_NAME = "twitter_link"
        private const val REQ_ID = "reqId"
        private const val SAME_STORY_TIMELIMIT = 60L * 60L * 6L
        private var deferredActions = hashMapOf<String, Deferred<Unit>>()
    }

}