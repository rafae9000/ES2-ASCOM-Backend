package com.ES2.ASCOM.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;

import org.apache.tomcat.util.http.fileupload.FileUpload;
import org.apache.tomcat.util.http.fileupload.FileUploadBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import com.ES2.ASCOM.exception.ApiRequestException;
import com.ES2.ASCOM.helpers.TokenService;
import com.ES2.ASCOM.model.ArquivoClippingDAO;
import com.ES2.ASCOM.model.Clipping;
import com.ES2.ASCOM.model.Usuario;
import com.ES2.ASCOM.repository.ClippingDAO;
import com.ES2.ASCOM.repository.UsuarioDAO;

@RestController
@RequestMapping("/clipping")
public class ClippingController {

	@Autowired
	private ArquivoClippingDAO arquivoClipingDAO;
	@Autowired
	private ClippingDAO clippingDAO;
	@Autowired
	private UsuarioDAO usuarioDAO;
	
	private TokenService tokenService;
	
	@GetMapping("/{id}")
	public Clipping achar(@PathVariable Integer id, @RequestHeader Map<String, String> header) throws ApiRequestException {
		String token = header.get("token");
		if (token == null)
			throw new ApiRequestException("Token não foi informado", HttpStatus.BAD_REQUEST);

		Integer logged_user_id = tokenService.getTokenSubject(token);
		Usuario logged_user = usuarioDAO.findById(logged_user_id).get();
		
		if(!logged_user.isAtivo())
			throw new ApiRequestException("Sua conta foi recentemente desativada pelo administrador", HttpStatus.FORBIDDEN);
		
		Optional<Clipping> aux = clippingDAO.findById(id);
		if(!aux.isPresent())
			throw new ApiRequestException("Não existe clipping com id = "+id, HttpStatus.BAD_REQUEST);
		
		Clipping clipping = aux.get();
		
		return clipping;
	}
	
	@PostMapping("/{id}/adicionarArquivo")
	public Map<String,String> addArquivo(@PathVariable Integer id, @RequestParam("arquivo") MultipartFile multipartFile)
			throws IOException, ApiRequestException {
        
		Optional<Clipping> aux = clippingDAO.findById(id);
		if(!aux.isPresent())
			throw new ApiRequestException("Não existe clipping com id = "+id, HttpStatus.BAD_REQUEST);
		
		String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
		String uploadDir = "clipping-arquivos/" + id; 
		saveFile(uploadDir, fileName, multipartFile);
        // procurar o codigo para achar o path de onde o jar esta
        
		return null;
   }
	
	
	
	
	private void saveFile(String uploadDir, String fileName,
            MultipartFile multipartFile) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
         
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
         
        try (InputStream inputStream = multipartFile.getInputStream()) {
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioe) {        
            throw new IOException("Could not save image file: " + fileName, ioe);
        }      
    }

}
