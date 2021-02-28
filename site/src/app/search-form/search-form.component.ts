import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import {FormGroup, FormControl} from '@angular/forms';
import {MatAccordion} from '@angular/material/expansion';

@Component({
  selector: 'app-search-form',
  templateUrl: './search-form.component.html',
  styleUrls: ['./search-form.component.css']
})
export class SearchFormComponent implements OnInit {
  query: string;
  bylines: string;
  source: string;
  range = new FormGroup({
    start: new FormControl(),
    end: new FormControl()
  });
  accordionOpened = false;

  @ViewChild(MatAccordion) accordion: MatAccordion;

  constructor(private route: ActivatedRoute) { }

  toggleAccordion() {
    this.accordionOpened = !this.accordionOpened;
    this.accordionOpened ? this.accordion.openAll() : this.accordion.closeAll();
  }

  ngOnInit(): void {
    this.route.queryParamMap.subscribe((params) => {
      this.query = params.get('q');
      this.bylines = params.get('b');
      this.source = params.get('s');
    });
  }

}
