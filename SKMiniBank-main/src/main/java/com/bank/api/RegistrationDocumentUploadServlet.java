package com.bank.api;

import com.bank.dao.OnboardingDAO;
import com.bank.integration.aws.AwsS3Storage;
import com.bank.model.CustomerDocument;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@WebServlet("/api/registration/documents/upload")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10,      // 10MB
        maxRequestSize = 1024 * 1024 * 50)   // 50MB
public class RegistrationDocumentUploadServlet extends HttpServlet {

    private final OnboardingDAO dao = new OnboardingDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String documentType = request.getParameter("documentType");
            String appRef = request.getParameter("applicationRef");

            if (documentType == null || documentType.trim().isEmpty()) {
                response.setStatus(400);
                out.print("{\"status\":\"failed\",\"message\":\"Document type required.\"}");
                return;
            }

            Part filePart = request.getPart("file");
            if (filePart == null || filePart.getSize() == 0) {
                response.setStatus(400);
                out.print("{\"status\":\"failed\",\"message\":\"No file selected.\"}");
                return;
            }

            String mime = filePart.getContentType();
            if (mime == null || (!mime.contains("image") && !mime.contains("pdf"))) {
                response.setStatus(400);
                out.print("{\"status\":\"failed\",\"message\":\"Only JPG, PNG, and PDF files are permitted.\"}");
                return;
            }

            String fileName = getFileName(filePart);
            String extension = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")) : ".png";
            String s3Key = "documents/" + (appRef != null ? appRef : "TEMP") + "_" + documentType + "_" + System.currentTimeMillis() + extension;

            // Save to local secure uploads directory & S3 storage
            String uploadPath = getServletContext().getRealPath("") + File.separator + "uploads" + File.separator + "documents";
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) uploadDir.mkdirs();

            File savedFile = new File(uploadPath + File.separator + s3Key.replace("/", "_"));
            filePart.write(savedFile.getAbsolutePath());

            try {
                AwsS3Storage.upload(savedFile.toPath(), s3Key, mime);
            } catch (Exception awsEx) {
                System.err.println("S3 Upload fallback: " + awsEx.getMessage());
            }

            CustomerDocument doc = new CustomerDocument();
            doc.setApplicationRef(appRef != null ? appRef : "PENDING");
            doc.setDocumentType(documentType);
            doc.setS3Bucket(System.getenv("AWS_S3_BUCKET") != null ? System.getenv("AWS_S3_BUCKET") : "skminibank-documents");
            doc.setS3Key(s3Key);
            doc.setFileName(fileName);
            doc.setMimeType(mime);
            doc.setFileSize((int) filePart.getSize());
            doc.setStatus("UPLOADED");

            dao.saveDocument(doc);

            out.print("{\"status\":\"success\",\"message\":\"Document uploaded successfully!\",\"s3Key\":\"" + s3Key + "\"}");

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            out.print("{\"status\":\"error\",\"message\":\"Document upload failed.\"}");
        } finally {
            out.flush();
            out.close();
        }
    }

    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] tokens = contentDisp.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2, token.length() - 1);
            }
        }
        return "document.png";
    }
}
