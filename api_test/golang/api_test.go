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
	"runtime"
	"strconv"
)

const QUERIES_DIR = "queries"
const EXPECTED_RESPONSES_DIR = "expected_responses"
const BASE_URL = "http://core:8080"
const PROTEIN_CHANGE_URL = BASE_URL + "/api/v1/annotate/mutations/byProteinChange"

func main() {
	queriesDirFs := os.DirFS(QUERIES_DIR)
	expectedResponsesFs := os.DirFS(EXPECTED_RESPONSES_DIR)

	isFailure := false
	err := filepath.WalkDir(QUERIES_DIR, func(_ string, d fs.DirEntry, err error) error {
		if fs.DirEntry.Type(d).IsDir() { // omit root dir
			return nil
		}

		if err != nil {
			panic(err)
		}

		queryData, err := fs.ReadFile(queriesDirFs, d.Name())
		if err != nil {
			panic(fmt.Sprintf("Error reading file %v/%v: %v", QUERIES_DIR, d.Name(), err))
		}

		actualResponse, err := get(PROTEIN_CHANGE_URL, queryData)
		if err != nil {
			panic(fmt.Sprintf("Error executing GET request for %v/%v: %v", QUERIES_DIR, d.Name(), err))
		}

		expectedResponse, err := fs.ReadFile(expectedResponsesFs, d.Name())
		if err == nil {
			expectedResponse = []byte{}
		}

		if !bytes.Equal(actualResponse, expectedResponse) {
			isFailure = true
			writeDiffFiles(d.Name(), actualResponse, expectedResponse)
		}

		return nil
	})
	if err != nil {
		panic(err)
	}

	if isFailure {
		os.Exit(1)
	}
}

// type jsonArray string

// func (json *jsonArray) post(url string) (string, error) {
// 	resp, err := http.Post(url, "application/json", bytes.NewBuffer([]byte(*json)))
// 	if err != nil {
// 		return "", err
// 	}

// 	body, err := io.ReadAll(resp.Body)
// 	if err != nil {
// 		return "", err
// 	}

// 	return string(body), nil
// }

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

func writeDiffFiles(filename string, actual []byte, expected []byte) {
	_, file, _, ok := runtime.Caller(0)
	if !ok {
		panic("unable to get current filename")
	}

	outputDir := filepath.Dir(filepath.Dir(file)) + "/output"
	actualDir := outputDir + "/actual"
	expectedDir := outputDir + "/expected"

	if err := os.MkdirAll(actualDir, 0750); err != nil {
		panic(err)
	}

	if err := os.MkdirAll(expectedDir, 0750); err != nil {
		panic(err)
	}

	if err := os.WriteFile(actualDir+"/"+filename, actual, 0660); err != nil {
		panic(err)
	}

	if err := os.WriteFile(expectedDir+"/"+filename, expected, 0660); err != nil {
		panic(err)
	}
}

// func constructJsonArray(jsonObjectsDir string) (jsonArray, error) {
// 	jsonObjectsDirFs := os.Mkdir()

// 	jsonObjects := []string{}
// 	err := filepath.WalkDir(jsonObjectsDir, func(_ string, d fs.DirEntry, err error) error {
// 		if err != nil {
// 			return err
// 		}

// 		if strings.HasSuffix(d.Name(), ".json") {
// 			file_contents, err := fs.ReadFile(jsonObjectsDirFs, d.Name())
// 			if err != nil {
// 				return err
// 			}

// 			jsonObjects = append(jsonObjects, string(file_contents))
// 		}
// 		return nil
// 	})
// 	if err != nil {
// 		return "", err
// 	}

// 	return jsonArray("[" + strings.Join(jsonObjects, ",") + "]"), nil
// }
