plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
    alias(libs.plugins.skie) apply false
    alias(libs.plugins.googleServices) apply false
    alias(libs.plugins.firebaseCrashlytics) apply false
}

// Generate mock Firebase configuration files if they are missing
val googleServicesJson = file("androidApp/app/google-services.json")
if (!googleServicesJson.exists()) {
    googleServicesJson.parentFile.mkdirs()
    googleServicesJson.writeText(
        """
        {
          "project_info": {
            "project_number": "1234567890",
            "project_id": "mock-project-id",
            "storage_bucket": "mock-bucket.appspot.com"
          },
          "client": [
            {
              "client_info": {
                "mobilesdk_app_id": "1:1234567890:android:mockappid",
                "android_client_info": {
                  "package_name": "com.cineverse.android"
                }
              },
              "oauth_client": [],
              "api_key": [
                {
                  "current_key": "mock_api_key"
                }
              ],
              "services": {
                "appinvite_service": {
                  "other_platform_oauth_client": []
                }
              }
            }
          ],
          "configuration_version": "1"
        }
        """.trimIndent()
    )
    logger.lifecycle("Created mock google-services.json at ${googleServicesJson.absolutePath}")
}

val googleServiceInfo = file("iosApp/GoogleService-Info.plist")
if (!googleServiceInfo.exists()) {
    googleServiceInfo.parentFile.mkdirs()
    googleServiceInfo.writeText(
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
        <plist version="1.0">
        <dict>
        	<key>API_KEY</key>
        	<string>mock_api_key</string>
        	<key>GCM_SENDER_ID</key>
        	<string>1234567890</string>
        	<key>PLIST_VERSION</key>
        	<string>1</string>
        	<key>BUNDLE_ID</key>
        	<string>com.cineverse.iosApp</string>
        	<key>PROJECT_ID</key>
        	<string>mock-project-id</string>
        	<key>STORAGE_BUCKET</key>
        	<string>mock-bucket.appspot.com</string>
        	<key>IS_ADS_ENABLED</key>
        	<false/>
        	<key>IS_ANALYTICS_ENABLED</key>
        	<false/>
        	<key>IS_APPINVITE_ENABLED</key>
        	<false/>
        	<key>IS_GCM_ENABLED</key>
        	<false/>
        	<key>IS_SIGNIN_ENABLED</key>
        	<false/>
        	<key>GOOGLE_APP_ID</key>
        	<string>1:1234567890:ios:mockappid</string>
        </dict>
        </plist>
        """.trimIndent()
    )
    logger.lifecycle("Created mock GoogleService-Info.plist at ${googleServiceInfo.absolutePath}")
}

