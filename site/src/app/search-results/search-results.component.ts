import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute, NavigationExtras } from '@angular/router';
import { NewsSearchApiService } from '../news-search-api.service';

@Component({
  selector: 'app-search-results',
  templateUrl: './search-results.component.html',
  styleUrls: ['./search-results.component.css']
})
export class SearchResultsComponent implements OnInit {
  mArticles: Array<any>;
  mSources: Map<string, number> = new Map<string, number>();
  timing: any;
  selectedSources: any;
  queryParams: any;
  static clearSources = true;
  topNews: boolean = false;
  topics: boolean = false;

  constructor(private router: Router, private route: ActivatedRoute, private searchApi: NewsSearchApiService) {}

  search(query, bylines, source, start = null, end = null, sources = null) {
    this.searchApi.getSearchResults(query, bylines, source, start, end, sources).subscribe(data => { 
      const allData = data as Array<any>;
      this.timing = allData["timing"];
      const groupData = allData["results"].filter(i => { return i.id.startsWith("group:root") });
      this.mArticles = allData["results"].filter(result => {
        return result.id.startsWith("id:newsarticle");
      });
      const test = new Map<string, number>();
      if (SearchResultsComponent.clearSources) {
        this.mSources.clear();
        this.selectedSources = null;
        groupData[0].children.children.forEach(item => {
          test.set(item.value, item.fields["count()"]);
        });
        this.mSources = test;
      } else {
        SearchResultsComponent.clearSources = true;
      }
    });
  }

  top() {
    this.topNews = true;
    this.searchApi.getTopNews().subscribe(data => {
      const allData = data as Array<any>;
      this.timing = allData["timing"];
      const groupData = allData["results"].filter(i => { return i.id.startsWith("group:root") });
      this.mArticles = allData["results"].filter(result => {
        return result.id.startsWith("id:newsarticle");
      });
      this.mSources.clear();
    });
  }

  related(id: String) {
    this.searchApi.getRelated(id).subscribe(data => {
      const allData = data as Array<any>;
      this.timing = allData["timing"];
      const groupData = allData["results"].filter(i => { return i.id.startsWith("group:root") });
      this.mArticles = allData["results"].filter(result => {
        return result.id.startsWith("id:newsarticle");
      });
      this.mSources.clear();
    });
  }

  selectedSearch() {
    let queryParams = Object.assign([], this.route.snapshot.queryParams);
    queryParams.sources = JSON.stringify(this.selectedSources);
    const navigationExtras: NavigationExtras = {
      queryParams
    };
    SearchResultsComponent.clearSources = false;
    this.router.navigate(['/search'], navigationExtras);
  }

  doSearch(queryParams) {
    if (queryParams['r'] && queryParams['e']) {
      var end = new Date(+queryParams['e']);
      end.setHours(23, 59, 59);
      var start = new Date(+queryParams['r']);
      start.setHours(0, 0, 0);
      this.search(queryParams['q'], queryParams['b'], queryParams['s'], start, end, queryParams['sources']);
    } else {
      this.search(queryParams['q'], queryParams['b'], queryParams['s'], null, null, queryParams['sources']);
    }
  }

  ngOnInit(): void {
    this.topNews = false;
    this.topics = false;
    this.route.queryParams.subscribe(queryParams => {
      this.queryParams = queryParams;
      if (queryParams['id'] != null) {
        this.related(queryParams['id']);
      } else if (queryParams['topic'] != null) {
        this.topics = true;
        this.search("group_doc_id:" + queryParams['topic'], null, null)
      } else if (queryParams['q'] == null) {
        this.top();
      } else {
        this.doSearch(queryParams);
      }
    });
  }

}
