import { Injectable } from '@angular/core';
import { HttpClient  } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class NewsSearchApiService {
  constructor(private http:HttpClient) { }

  getTopNews() {
    const url = "https://search.ausnews.org/search?q=twitter_favourite_count%3A%3E1&h=20&ranking=twitter&twitterWeight=0.2&twitterRetweetWeight=0.2&twitterFavouriteWeight=0.1&freshnessWeight=50"
    return this.http.get(url);
  }

  getSearchResults(query: String, bylines: String, source: String = null, start: Date, end: Date, sources: string) {
    var url = `https://search.ausnews.org/search?q=${query}&h=20`;
    if (bylines != null && bylines.length > 0) {
      url += `&b=${bylines}`;
    }
    if (source && source.length > 0) {
      url += `&s=${source}`;
    }
    if (start && end) {
      url += `&r=${start.getTime() / 1000}&e=${end.getTime() / 1000}`;
    }
    if (sources && sources.length > 0) {
      const mSources = JSON.parse(sources);
      if (mSources.length > 0) {
        url += "&source=" + JSON.parse(sources).join("&source=");
      }
    }
    return this.http.get(url);
  }

  getRelated(id: String) {
    var url = `https://search.ausnews.org/search/related?id=${id}`;
    return this.http.get(url);
  }
}