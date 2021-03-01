import { Component, OnInit, Pipe, PipeTransform } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { NewsSearchApiService } from '../news-search-api.service';

@Component({
  selector: 'app-news-topics',
  templateUrl: './news-topics.component.html',
  styleUrls: ['./news-topics.component.css']
})
export class NewsTopicsComponent implements OnInit {
  mTopics: Array<any>;
  JSON: JSON;

  constructor(private router: Router, private route: ActivatedRoute, private searchApi: NewsSearchApiService) {
    this.JSON = JSON
  }

  ngOnInit(): void {
    this.searchApi.getTopics().subscribe(data => {
      const allData = data as Array<any>;
      this.mTopics = allData["results"][0]["children"]["children"]
      this.mTopics.forEach(topic => {
        topic.children[0].children.forEach(article => {
          if (article.fields.abstract) {
            article.fields.abstract = article.fields.abstract.replace(/<.*?>/g, '');
          }
        })
      })
    })
  }
}

