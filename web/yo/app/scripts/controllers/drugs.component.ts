import { Component, OnInit } from '@angular/core';
import { Http, Response} from '@angular/http';
import 'rxjs/Rx';
import 'rxjs/add/operator/map';

@Component({
    selector: 'drugs-component',
    templateUrl: '../../views/drugs.component.html'
})

export class DrugsComponent implements OnInit {
    userRole: number;
    allDrugs: object;
    rendering: boolean;
    rowsOnPage: number;
    sortBy: string;
    sortOrder: string;


    constructor(private http: Http) {
        this.userRole = 8;
        this.rendering = false;
        this.rowsOnPage = 10;
        this.sortBy = 'drugName'
        this.sortOrder = "asc";
    }

    ngOnInit(): void {
        this.getJSON('../../../data/drugs.json');
    }

    public getJSON(url:string) {
        return this.http.get(url)
            .map((res:Response) => res.json())
            .subscribe(result => {
                this.allDrugs = result;
                this.rendering = true;
            }, error => {
                this.rendering = false;
                console.log('error happened when loading all drugs information', error)
            });
    }
}


