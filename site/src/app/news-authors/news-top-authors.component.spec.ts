import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SearchTopAuthorsComponent } from './news-top-authors.component';

describe('NewsTopicsComponent', () => {
  let component: SearchTopAuthorsComponent;
  let fixture: ComponentFixture<SearchTopAuthorsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SearchTopAuthorsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchTopAuthorsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
