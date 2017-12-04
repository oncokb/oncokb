"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var core_1 = require("@angular/core");
var http_1 = require("@angular/http");
require("rxjs/Rx");
require("rxjs/add/operator/map");
var DrugsComponent = (function () {
    function DrugsComponent(http) {
        this.http = http;
        this.userRole = 8;
        this.rendering = false;
    }
    DrugsComponent.prototype.ngOnInit = function () {
        this.getJSON('../../../data/drugs.json');
    };
    DrugsComponent.prototype.getJSON = function (url) {
        var _this = this;
        return this.http.get(url)
            .map(function (res) { return res.json(); })
            .subscribe(function (result) {
            _this.allDrugs = result;
            _this.rendering = true;
        }, function (error) {
            _this.rendering = false;
            console.log('error happened when loading all drugs information', error);
        });
    };
    return DrugsComponent;
}());
DrugsComponent = __decorate([
    core_1.Component({
        selector: 'drugs-component',
        templateUrl: '../../views/drugs.component.html'
    }),
    __metadata("design:paramtypes", [http_1.Http])
], DrugsComponent);
exports.DrugsComponent = DrugsComponent;
//# sourceMappingURL=drugs.component.js.map