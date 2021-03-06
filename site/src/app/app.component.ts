import { Component, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import {FormGroup, FormControl} from '@angular/forms';
import {MatAccordion} from '@angular/material/expansion';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'AUSNews Search';
  links: {[key: string]: string} = {'Top News': '', 'Top Articles': '/articles', 'Top Authors': '/authors'}
  activeLink = this.links['Top News'];
  
  constructor(private route: ActivatedRoute) {}
  
  ngOnInit(): void {
  }

  sortLinks(a, b): number {
    return a.key == "Top News" ? -1 : 1;
  }
}
