import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpModule } from '@angular/http';
import { BrowserModule } from '@angular/platform-browser';
import { UpgradeModule } from '@angular/upgrade/static';
import { RouterModule, Routes } from '@angular/router';
import { DataTableModule } from 'angular2-datatable';

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
        DataTableModule,
        RouterModule.forChild(appRoutes)
    ],
    declarations: [ DrugsComponent ]
})
export class DrugsModule { }
