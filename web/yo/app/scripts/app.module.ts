import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { UpgradeModule } from '@angular/upgrade/static';
import { RouterModule, Routes, UrlHandlingStrategy, UrlTree } from '@angular/router';
import { HashLocationStrategy, LocationStrategy, CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { DataTableModule } from 'angular2-datatable';

import { AppComponent } from './app.component.js';
import { DrugsModule } from './controllers/drugs.module.js';

class HybridUrlHandlingStrategy implements UrlHandlingStrategy {
    // use only process the `/drugs` url
    shouldProcessUrl(url: UrlTree) {
        return url.toString().startsWith('/drugs');
    }
    extract(url: UrlTree) { return url; }
    merge(url: UrlTree, whole: UrlTree) { return url; }
}

@NgModule({
    imports: [
        CommonModule,
        BrowserModule,
        UpgradeModule,
        HttpClientModule,
        RouterModule.forRoot([], { initialNavigation: false }),
        DataTableModule,
        DrugsModule
    ],
    providers: [
        { provide: LocationStrategy, useClass: HashLocationStrategy },
        { provide: UrlHandlingStrategy, useClass: HybridUrlHandlingStrategy }
    ],
    declarations: [ AppComponent ],
    bootstrap: [ AppComponent ]
})
export class AppModule {
    // ngDoBootstrap() {}
}
