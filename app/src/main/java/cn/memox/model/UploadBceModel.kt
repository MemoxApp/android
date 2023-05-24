package cn.memox.model

import cn.memox.ui.screen.add.memory.UploadImageState
import cn.memox.utils.appUserAgent
import com.baidubce.auth.DefaultBceSessionCredentials
import com.baidubce.services.bos.BosClient
import com.baidubce.services.bos.BosClientConfiguration
import com.baidubce.services.bos.callback.BosProgressCallback
import com.baidubce.services.bos.model.PutObjectRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.io.File

object UploadBceModel {
    private const val endpoint = "https://su.bcebos.com"
    fun upload(
        scope: CoroutineScope,
        file: File,
        accessKey: String,
        sessionKey: String,
        sessionToken: String,
        key: String
    ) = channelFlow<UploadImageState> {
        val config = BosClientConfiguration().apply {
            endpoint = this@UploadBceModel.endpoint
            credentials = DefaultBceSessionCredentials(accessKey, sessionKey, sessionToken)
            userAgent = appUserAgent
        }
        val client = BosClient(config)
        try {
            val resp = client.putObject(PutObjectRequest("timespeak", key, file).apply {
                setProgressCallback(object : BosProgressCallback<PutObjectRequest>() {
                    override fun onProgress(
                        request: PutObjectRequest?,
                        currentSize: Long,
                        totalSize: Long
                    ) {
                        super.onProgress(request, currentSize, totalSize)
                        scope.launch {
                            send(
                                UploadImageState.Uploading(
                                    currentSize.toFloat() / totalSize.toFloat()
                                )
                            )
                        }
                    }
                })
            })
            if (resp.httpResponse.statusCode == 200) {
                send(UploadImageState.Success)
            } else {
                send(UploadImageState.Error("上传失败"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println(e)
            send(UploadImageState.Error("上传失败,$e"))
        }
    }.flowOn(Dispatchers.IO)
}

