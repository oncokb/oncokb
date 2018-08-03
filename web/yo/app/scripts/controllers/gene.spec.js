describe('Gene Controller', function () {
	beforeEach(module('oncokbApp'));
	beforeEach(inject(function (_$controller_, _$rootScope_) {
		$rootScope = _$rootScope_;
		$scope = $rootScope.$new();
		$controller = _$controller_;
		controller = $controller('GeneCtrl', { $scope: $scope });
		$scope.meta.gene = {
			entrezGeneId: 207
		};
		$scope.gene = OncoKB.gene;
		$scope.geneMeta = {
			review: {
				currentReviewer: ''
			}
		};
	}));
	it('The name section style is wrong', function () {
		$scope.reviewMode = false;
		expect($scope.getNameStyle()).toEqual({ float: 'left' });
		expect($scope.getNameStyle('mutation')).toEqual({ float: 'left' });
		$scope.reviewMode = true;
		expect($scope.getNameStyle()).toEqual(null);
		expect($scope.getNameStyle('mutation')).toEqual({ 'margin-top': '20px' });
	});
	it('Developer check is not working', function () {
		$rootScope.me = { name: 'Jiaojiao wang' };
		expect($scope.developerCheck()).toEqual(true);

		$rootScope.me = { name: 'cBioPortal' };
		expect($scope.developerCheck()).toEqual(false);
	});
	it('Check if a removed section should be displayed is not working', function () {
		$rootScope.reviewMode = false;
		expect($scope.displayCheck()).toEqual(true);
		var uuid = 'ef3e7a30-73e5-41b0-8b3e-f9f08b92c50a';
		var reviewObj = {
			removed: true,
			updatedBy: 'Jiaojiao wang',
			updateTime: 1524594735169
		}
		expect($scope.displayCheck(uuid, reviewObj)).toEqual(false);
		$rootScope.reviewMode = true;
		$scope.sectionUUIDs = [uuid];
		expect($scope.displayCheck(uuid, reviewObj)).toEqual(true);
	});
	describe('Prepare the evidence model and history model for the review mode API call', function () {
		var data = {
			additionalInfo: null,
			alterations: null,
			cancerType: null,
			description: null,
			evidenceType: '',
			gene: {
				hugoSymbol: '',
				entrezGeneId: ''
			},
			knownEffect: null,
			lastEdit: null,
			levelOfEvidence: null,
			subtype: null,
			articles: [],
			treatments: null,
			propagation: null
		};
		// Test the function to prepare evidence model for API call in each type
		it('GENE_SUMMARY type is wrong', function () {
			var evidenceItem = angular.copy(data);
			evidenceItem.evidenceType = 'GENE_SUMMARY';
			evidenceItem.description = $scope.gene.summary;
			evidenceItem.gene = {
				hugoSymbol: $scope.gene.name,
				entrezGeneId: $scope.meta.gene.entrezGeneId
			};
			evidenceItem.lastEdit = $scope.gene.summary_review.updateTime.toString();
			var evidencesResult = {};
			evidencesResult[$scope.gene.summary_uuid] = evidenceItem;
			var result = {
				evidences: evidencesResult,
				historyData: {
					operation: 'update',
					location: 'Gene Summary',
                    new: $scope.gene.summary,
                    old: $scope.gene.summary_review.lastReviewed,
					uuids: $scope.gene.summary_uuid,
					lastEditBy: $scope.gene.summary_review.updatedBy
				}
			};
			expect($scope.getEvidence('GENE_SUMMARY')).toEqual(result);
		});
		it('GENE_BACKGROUND type is wrong', function () {
			var evidenceItem = angular.copy(data);
			evidenceItem.evidenceType = 'GENE_BACKGROUND';
			evidenceItem.description = $scope.gene.background;
			evidenceItem.articles = [{ pmid: '28431241' }, { pmid: '9843996' }, { pmid: '7611497' }, { pmid: '17611497' }, { pmid: '23134728' }, { pmid: '20440266' }, { pmid: '18767981' }, { pmid: '28489509' }, { pmid: '29535262' }, { pmid: '29339542' }];
			evidenceItem.gene = {
				hugoSymbol: $scope.gene.name,
				entrezGeneId: $scope.meta.gene.entrezGeneId
			};
			evidenceItem.lastEdit = $scope.gene.background_review.updateTime.toString();
			var evidencesResult = {};
			evidencesResult[$scope.gene.background_uuid] = evidenceItem;
			var result = {
				evidences: evidencesResult,
				historyData: {
					operation: 'update',
					location: 'Gene Background',
                    new: $scope.gene.background,
                    old: $scope.gene.background_review.lastReviewed,
					uuids: $scope.gene.background_uuid,
					lastEditBy: $scope.gene.background_review.updatedBy
				}
			};
			expect($scope.getEvidence('GENE_BACKGROUND')).toEqual(result);
		});
	});
});

