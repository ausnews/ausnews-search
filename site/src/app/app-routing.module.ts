import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SearchResultsComponent } from './search-results/search-results.component';
import { NewsTopicsComponent } from './news-topics/news-topics.component';
import { SearchFormComponent } from './search-form/search-form.component';

const routes: Routes = [
  { path: '', component: NewsTopicsComponent },
  { path: 'articles', component: SearchResultsComponent },
  { path: 'search', component: SearchResultsComponent },
  { path: 'searchf', component: SearchFormComponent },
  { path: 'search/related', component: SearchResultsComponent },
  { path: 'search/topic', component: SearchResultsComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }