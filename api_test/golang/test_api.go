package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"io/fs"
	"net/http"
	"net/url"
	"os"
	"path/filepath"
	"strconv"
)

const QUERIES_DIR = "queries"
const PROTEIN_CHANGE_DIR = QUERIES_DIR + "/protein_change"
const GENOMIC_CHANGE_DIR = QUERIES_DIR + "/genomic_change"
const HGVSG_DIR = QUERIES_DIR + "/hgvsg"
const GERMLINE_HGVSC_DIR = QUERIES_DIR + "/germline_hgvsc"
const GERMLINE_HGVSG_DIR = QUERIES_DIR + "/germline_hgvsg"
const GERMLINE_GENOMIC_CHANGE_DIR = QUERIES_DIR + "/germline_genomic_change"
const EXPECTED_RESPONSES_DIR = "expected_responses"

const BASE_URL = "http://core:8080"
const PROTEIN_CHANGE_URL = BASE_URL + "/api/v1/annotate/mutations/byProteinChange"
const GENOMIC_LOCATION_URL = BASE_URL + "/api/v1/annotate/mutations/byGenomicChange"
const HGVSG_URL = BASE_URL + "/api/v1/annotate/mutations/byHGVSg"
const GERMLINE_HGVSC_URL = BASE_URL + "/api/v1/annotate/germline/mutations/byHGVSc"
const GERMLINE_HGVSG_URL = BASE_URL + "/api/v1/annotate/germline/mutations/byHGVSg"
const GERMLINE_GENOMIC_CHANGE_URL = BASE_URL + "/api/v1/annotate/germline/mutations/byGenomicChange"

func main() {
	proteinChangeFailure := runQueriesAndReportFailure(PROTEIN_CHANGE_DIR, PROTEIN_CHANGE_URL)
	genomicLocationFailure := runQueriesAndReportFailure(GENOMIC_CHANGE_DIR, GENOMIC_LOCATION_URL)
	hgvsgFailure := runQueriesAndReportFailure(HGVSG_DIR, HGVSG_URL)
	germlineHgvscFailure := runQueriesAndReportFailure(GERMLINE_HGVSC_DIR, GERMLINE_HGVSC_URL)
	germlineHgvsgFailure := runQueriesAndReportFailure(GERMLINE_HGVSG_DIR, GERMLINE_HGVSG_URL)
	germlineGenomicChangeFailure := runQueriesAndReportFailure(GERMLINE_GENOMIC_CHANGE_DIR, GERMLINE_GENOMIC_CHANGE_URL)
	if proteinChangeFailure || genomicLocationFailure || hgvsgFailure || germlineHgvscFailure || germlineHgvsgFailure || germlineGenomicChangeFailure {
		os.Exit(1)
	}
}

func runQueriesAndReportFailure(dir string, url string) bool {
	category := filepath.Base(dir)
	dirFs := os.DirFS(dir)
	expectedResponsesFs := os.DirFS(EXPECTED_RESPONSES_DIR + "/" + category)

	isFailure := false

	err := filepath.WalkDir(dir, func(_ string, d fs.DirEntry, err error) error {
		if fs.DirEntry.Type(d).IsDir() { // omit root dir
			return nil
		}

		if err != nil {
			panic(err)
		}

		queryData, err := fs.ReadFile(dirFs, d.Name())
		if err != nil {
			panic(fmt.Sprintf("Error reading file %v/%v: %v", dir, d.Name(), err))
		}

		actualResponse, err := get(url, queryData)
		if err != nil {
			panic(fmt.Sprintf("Error executing GET request for %v/%v: %v", dir, d.Name(), err))
		}

		expectedResponse, err := fs.ReadFile(expectedResponsesFs, d.Name())
		if err != nil {
			expectedResponse = []byte{}
		}

		if !bytes.Equal(actualResponse, expectedResponse) {
			isFailure = true
			writeDiffFiles(category, d.Name(), actualResponse, expectedResponse)
		}

		return nil
	})
	if err != nil {
		panic(err)
	}

	return isFailure
}

func get(requestUrl string, queryData []byte) ([]byte, error) {
	var jsonMap map[string]any
	err := json.Unmarshal(queryData, &jsonMap)
	if err != nil {
		return nil, err
	}

	u, err := url.Parse(requestUrl)
	if err != nil {
		return nil, err
	}

	query := u.Query()
	for key, val := range jsonMap {
		switch t := val.(type) {
		case float64:
			query.Add(key, strconv.FormatFloat(val.(float64), 'f', -1, 64))
		case string:
			query.Add(key, val.(string))
		default:
			return nil, fmt.Errorf(`key "%v" unexpected type "%T" in query data`, key, t)
		}
	}
	u.RawQuery = query.Encode()

	resp, err := http.Get(u.String())
	if err != nil {
		return nil, err
	}

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, err
	}

	return body, nil
}

func writeDiffFiles(category string, filename string, actual []byte, expected []byte) {
	ex, err := os.Executable()
	if err != nil {
		panic(err)
	}

	outputDir := filepath.Dir(ex) + "/output"
	actualDir := outputDir + "/actual/" + category
	expectedDir := outputDir + "/expected/" + category

	if err := os.MkdirAll(actualDir, 0755); err != nil {
		panic(err)
	}

	if err := os.MkdirAll(expectedDir, 0755); err != nil {
		panic(err)
	}

	if err := os.WriteFile(actualDir+"/"+filename, actual, 0666); err != nil {
		panic(err)
	}

	if err := os.WriteFile(expectedDir+"/"+filename, expected, 0666); err != nil {
		panic(err)
	}
}
