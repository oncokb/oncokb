import { Component } from '@angular/core';

@Component({
    selector: 'app-root',
    template: ` <router-outlet></router-outlet>
    <div ng-view="" class="container oncokbMainView"></div>`,
})
export class AppComponent { }
