import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { NewsSearchApiService } from '../news-search-api.service';

@Component({
  selector: 'app-news-top-authors',
  templateUrl: './news-top-authors.component.html',
  styleUrls: ['./news-top-authors.component.css']
})
export class SearchTopAuthorsComponent implements OnInit {
  mAuthors: Array<any>;
  JSON: JSON;

  constructor(private router: Router, private route: ActivatedRoute, private searchApi: NewsSearchApiService) {
    this.JSON = JSON
  }

  ngOnInit(): void {
    this.searchApi.getAuthors().subscribe(data => {
      const allData = data as Array<any>;
      this.mAuthors = allData["results"][0]["children"]["children"]
      this.mAuthors.forEach(topic => {
        topic.children[0].children.forEach(article => {
          if (article.fields.abstract) {
            article.fields.abstract = article.fields.abstract.replace(/<.*?>/g, '');
          }
        })
      })
    })
  }
}

