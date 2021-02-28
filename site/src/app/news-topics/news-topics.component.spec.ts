import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NewsTopicsComponent } from './news-topics.component';

describe('NewsTopicsComponent', () => {
  let component: NewsTopicsComponent;
  let fixture: ComponentFixture<NewsTopicsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NewsTopicsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NewsTopicsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
