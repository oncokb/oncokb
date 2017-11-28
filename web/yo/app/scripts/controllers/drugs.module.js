"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var core_1 = require("@angular/core");
var common_1 = require("@angular/common");
var http_1 = require("@angular/http");
var platform_browser_1 = require("@angular/platform-browser");
var static_1 = require("@angular/upgrade/static");
var router_1 = require("@angular/router");
var drugs_component_js_1 = require("./drugs.component.js");
var appRoutes = [
    { path: 'drugs', component: drugs_component_js_1.DrugsComponent }
];
var DrugsModule = (function () {
    function DrugsModule() {
    }
    return DrugsModule;
}());
DrugsModule = __decorate([
    core_1.NgModule({
        imports: [
            common_1.CommonModule,
            http_1.HttpModule,
            platform_browser_1.BrowserModule,
            static_1.UpgradeModule,
            router_1.RouterModule.forChild(appRoutes)
        ],
        declarations: [drugs_component_js_1.DrugsComponent],
        bootstrap: []
    })
], DrugsModule);
exports.DrugsModule = DrugsModule;
//# sourceMappingURL=drugs.module.js.map