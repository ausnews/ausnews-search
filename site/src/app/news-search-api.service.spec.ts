import { TestBed } from '@angular/core/testing';

import { NewsSearchApiService } from './news-search-api.service';

describe('NewsSearchApiService', () => {
  let service: NewsSearchApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(NewsSearchApiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
