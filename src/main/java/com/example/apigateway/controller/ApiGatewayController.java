package com.example.apigateway.controller;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

/**
 * APIゲートウェイコントローラークラス
 * 
 * <p>AWS API Gatewayを経由してPDFファイルを取得する処理を提供します。</p>
 */
@Controller
public class ApiGatewayController {

    // application.propertiesからAWS API GatewayのURLを注入
    @Value("${aws.api.gateway.url}")
    private String awsApiGatewayUrl;

    // REST API呼び出し用のテンプレート
    @Autowired
    private RestTemplate restTemplate;

    /**
     * インデックスページを表示
     * 
     * <p>ルートパス("/")へのGETリクエストを処理し、PDFダウンロードページを表示します。</p>
     * 
     * @return PDFダウンロードページのテンプレート名
     */
    @GetMapping("/getPdf_OP3")
    public String showIndexPage() {
        return "pdfDownload"; // pdfDownload.htmlテンプレートを返す
    }

    /**
     * API Gateway経由でPDFファイルを取得
     * 
     * <p>AWS API Gatewayを経由してPDFファイルを取得し、ダウンロードレスポンスを返します。</p>
     * 
     * <p>処理フロー:</p>
     * <ol>
     *   <li>AWS API GatewayにGETリクエストを送信</li>
     *   <li>レスポンスをバイト配列で受け取る</li>
     *   <li>ファイル名をUTF-8でエンコード</li>
     *   <li>PDFファイルとしてダウンロードできるようレスポンスを構築</li>
     * </ol>
     * 
     * @return PDFファイルを含むレスポンスエンティティ
     * @throws UnsupportedEncodingException ファイル名エンコードに失敗した場合
     */
    @GetMapping("/ApiGateway")
    public ResponseEntity<InputStreamResource> getPdfApiGateway() throws UnsupportedEncodingException {
        // AWS API GatewayにGETリクエストを送信
        ResponseEntity<byte[]> response = restTemplate.exchange(
                awsApiGatewayUrl,
                HttpMethod.GET,
                null,
                byte[].class);

        // レスポンスチェック
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            // バイト配列から入力ストリームを作成
            ByteArrayInputStream bis = new ByteArrayInputStream(response.getBody());    
            InputStreamResource resource = new InputStreamResource(bis);
            
            // ファイル名をUTF-8でエンコード（スペースを%20に置換）
            String encodedFilename = URLEncoder.encode("絵で見て分かるJSBase開発.pdf", "UTF-8").replace("+", "%20"); 
            
            // レスポンスヘッダーを設定
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF); // PDFコンテンツタイプを設定
            headers.setContentDisposition(
                    ContentDisposition.attachment() // 添付ファイルとしてダウンロード
                            .filename(encodedFilename) // エンコード済みファイル名
                            .build());

            // 成功レスポンスを返す
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } else {
            // エラーの場合
            throw new RuntimeException("Failed to download PDF from remote server");
        }
    }
}
