import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpModule } from '@angular/http';
import { BrowserModule } from '@angular/platform-browser';
import { UpgradeModule } from '@angular/upgrade/static';
import { RouterModule, Routes } from '@angular/router';

import { DrugsComponent } from './drugs.component.js';

const appRoutes: Routes = [
    { path: 'drugs', component: DrugsComponent }
];

@NgModule({
    imports: [
        CommonModule,
        HttpModule,
        BrowserModule,
        UpgradeModule,
        RouterModule.forChild(appRoutes)
    ],
    declarations: [ DrugsComponent ],
    bootstrap: []
})
export class DrugsModule { }
