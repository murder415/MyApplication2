/*package com.example.myapplication2;


    private void loadImage() {
        GenerateImageTask task = new GenerateImageTask();
        task.start(url, apiKey, prompt, size, numImages, responseFormat);
    }

    private class GenerateImageTask extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... params) {
            String url = (String) params[0];
            String apiKey = (String) params[1];
            String prompt = (String) params[2];
            String size = (String) params[3];
            int numImages = (int) params[4];
            String responseFormat = (String) params[5];

            String responseUrl = "";

            try {
                // DALL-E API 호출
                String jsonString = "{ \"model\": \"image-alpha-001\", " +
                        "\"prompt\": \"" + prompt + "\", " +
                        "\"num_images\": " + numImages + ", " +
                        "\"size\": \"" + size + "\", " +
                        "\"response_format\": \"" + responseFormat + "\" }";
                InputStream inputStream = NetworkUtils.getResponseFromHttpUrl(url, jsonString, apiKey);
                String response = NetworkUtils.convertStreamToString(inputStream);

                responseUrl = JSONUtil.getUrlFromJson(response);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return responseUrl;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null && !result.isEmpty()) {
                Picasso.get()
                        .load(result)
                        .into(imageView);
            }
        }

        public void start(Object... params) {
            super.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        }
    }

    public static class NetworkUtils {

        public static String convertStreamToString(InputStream stream) {
            Scanner scanner = new Scanner(stream).useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }

        public static InputStream getResponseFromHttpUrl(String url, String jsonString, String apiKey) throws Exception {
            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, jsonString);
            Request request = new Request.Builder()
                    .url(url)
                    .method("POST", body)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .build();

            Response response = client.newCall(request).execute();

            if (response != null && response.isSuccessful() && response.body() != null) {
                return response.body().byteStream();
            } else {
                throw new Exception("Failed to get response from API");
            }
        }

    }

    public static class JSONUtil {

        public static String getUrlFromJson(String jsonString) {
            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                JSONObject dataObject = jsonArray.getJSONObject(0);
                String imageUrl = dataObject.getString("url");
                return imageUrl;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

}
*/
