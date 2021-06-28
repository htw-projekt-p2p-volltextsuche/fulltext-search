# SearchApi

All URIs are relative to *http://localhost:8421/api*

Method | HTTP request | Description
------------- | ------------- | -------------
[**postSearch**](SearchApi.md#postSearch) | **POST** /searches | Search speeches


<a name="postSearch"></a>
# **postSearch**
> inline_response_200 postSearch(Search)

Search speeches

    Retrieve the most relevant documents for the given search query.

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **Search** | [**Search**](../Models/Search.md)|  | [optional]

### Return type

[**inline_response_200**](../Models/inline_response_200.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

