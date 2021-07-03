package com.ES2.ASCOM.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.tomcat.util.http.fileupload.FileUpload;
import org.apache.tomcat.util.http.fileupload.FileUploadBase;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
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
import com.ES2.ASCOM.helpers.ConfigValues;
import com.ES2.ASCOM.helpers.TokenService;
import com.ES2.ASCOM.model.Clipping;
import com.ES2.ASCOM.model.ArquivoClipping;
import com.ES2.ASCOM.model.Usuario;
import com.ES2.ASCOM.repository.ArquivoClippingDAO;
import com.ES2.ASCOM.repository.ClippingDAO;
import com.ES2.ASCOM.repository.UsuarioDAO;

@RestController
@RequestMapping("/clipping")
public class ClippingController {

	private final int TAMANHOMAXIMO = 100000000;
	
	@Autowired
	private ArquivoClippingDAO arquivoClippingDAO;
	@Autowired
	private ClippingDAO clippingDAO;
	@Autowired
	private UsuarioDAO usuarioDAO;
	
	private TokenService tokenService;
	
	private ConfigValues configValues = new ConfigValues();
	
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
	public Map<String,String> addArquivo(@PathVariable Integer id, @RequestParam("arquivo") MultipartFile multipartFile, @RequestParam("sobreescrever") Boolean sobreescrever)
			throws ApiRequestException, IOException {
          
		Optional<Clipping> aux = clippingDAO.findById(id);
		if(!aux.isPresent())
			throw new ApiRequestException("Não existe clipping com id = "+id, HttpStatus.BAD_REQUEST);
		
		Clipping clipping = aux.get();
		
		String nomeArquivo = StringUtils.cleanPath(multipartFile.getOriginalFilename());
		if(nomeArquivo.length() > 50) throw new ApiRequestException("O nome do arquivo pode ter no maximo 50 caracteres", HttpStatus.BAD_REQUEST);
		
		Long ChecktamanhoArquivo = multipartFile.getSize();
		if(ChecktamanhoArquivo > TAMANHOMAXIMO) throw new ApiRequestException("O tamanho maximo aceito de um arquivo é de 100MB", HttpStatus.BAD_REQUEST);
		
		Integer tamanhoArquivo =  Math.toIntExact(ChecktamanhoArquivo);
		
		String uploadDir = this.configValues.getClippingFolder() + id + File.separator;
		this.configValues.createFolder(uploadDir);

		String caminho = uploadDir + nomeArquivo + File.separator;
		boolean arquivoExiste = this.configValues.folderExist(caminho);
		ArquivoClipping arquivoClipping = null;
		if(arquivoExiste) {
			arquivoClipping = arquivoClippingDAO.findByCaminhoAbsoluto(caminho).get();
			arquivoClipping.setTamanho(tamanhoArquivo);
		}
		else
			arquivoClipping = new ArquivoClipping(null,clipping,caminho,nomeArquivo,tamanhoArquivo);
		
		arquivoClippingDAO.save(arquivoClipping);
		saveFile(uploadDir, nomeArquivo, multipartFile, sobreescrever);
	
        // procurar o codigo para achar o path de onde o jar esta
		Map<String,String> result = new HashMap<String,String>();
		result.put("message","Arquivo enviado com sucesso");
		return result;
   }
	
	
	
	
	private void saveFile(String uploadDir, String nomeArquivo,
            MultipartFile multipartFile, Boolean sobreescrever) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        try (InputStream inputStream = multipartFile.getInputStream()) {
            Path filePath = uploadPath.resolve(nomeArquivo);
            // se filePath existe e sobreescrever é igual a falso então lançar exceção ApiRequestException
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioe) {        
            throw new IOException("Could not save image file: " + nomeArquivo, ioe);
        }      
    }

}
