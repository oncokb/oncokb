oncokbApp.filter('getIndexByObjectNameInArray', function() {
    return function(array, attr, name) {
        for (var i = 0, arrayL = array.length; i < arrayL; i++) {
            if(array[i][attr] === name) {
                return i;
            }
        }
        return null;
    }
})