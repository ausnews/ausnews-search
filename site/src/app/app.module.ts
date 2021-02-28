import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';

import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import {MatCardModule} from '@angular/material/card';
import {MatDividerModule} from '@angular/material/divider';
import { FlexLayoutModule } from '@angular/flex-layout';
import { SearchResultsComponent } from './search-results/search-results.component';
import { AppRoutingModule } from './app-routing.module';
import {MatDatepickerModule, } from '@angular/material/datepicker';
import { ReactiveFormsModule } from '@angular/forms';
import { MatNativeDateModule } from '@angular/material/core';
import {MatExpansionModule} from '@angular/material/expansion';
import {MatListModule} from '@angular/material/list';
import { MAT_DATE_LOCALE } from '@angular/material/core';
import { NewsTopicsComponent } from './news-topics/news-topics.component';
import { MomentModule } from 'ngx-moment';
import {MatTabsModule} from '@angular/material/tabs';
import { SearchFormComponent } from './search-form/search-form.component';

@NgModule({
  declarations: [
    AppComponent,
    SearchResultsComponent,
    NewsTopicsComponent,
    SearchFormComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    MatToolbarModule,
    MatIconModule,
    MatButtonModule,
    MatInputModule,
    FormsModule,
    HttpClientModule,
    MatCardModule,
    MatDividerModule,
    FlexLayoutModule,
    AppRoutingModule,
    MatDatepickerModule,
    ReactiveFormsModule,
    MatNativeDateModule,
    MatExpansionModule,
    MatListModule,
    MomentModule,
    MatTabsModule
  ],
  providers: [MatDatepickerModule, { provide: MAT_DATE_LOCALE, useValue: 'en-AU' }],
  bootstrap: [AppComponent]
})
export class AppModule { }
