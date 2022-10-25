package com.generation.blogpessoal.service;

import java.nio.charset.Charset;
import java.util.Optional;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.generation.blogpessoal.model.Usuario;
import com.generation.blogpessoal.model.UsuarioLogin;
import com.generation.blogpessoal.repository.UsuarioRepository;

@Service
public class UsuarioService {
	
	
	@Autowired
	private UsuarioRepository usuarioRepository;
	
		//optional usuario esta importando da model, e o segundo usuario é objeto
		public Optional<Usuario> cadastrarUsuario(Usuario usuario){
			
			//if ver se ta presente o email q foi colocado, com oq ta no banco de dados, se nao tiver irá aparecer um erro
			if(usuarioRepository.findByUsuario(usuario.getUsuario()).isPresent())//is presente, é como um true or false
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"usuario já existe", null);
				
				usuario.setSenha(criptografarSenha(usuario.getSenha()));
				
				//optional of indica que está retornanddo um usuario q nao está mais vazio
			return Optional.of(usuarioRepository.save(usuario)); //ler de trás pra frente
		}
		
		public Optional<Usuario> atualizarUsuario(Usuario usuario){
	        
	        if (usuarioRepository.findById(usuario.getId()).isPresent()) {
	            Optional<Usuario> buscaUsuario =  usuarioRepository. 
	            findByUsuario(usuario.getUsuario());
	            if (buscaUsuario.isPresent()) {
	                if (buscaUsuario.get().getId() != usuario.getId()) 
	                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
	                            "Usuário já existe!", null);
	            }usuario.setSenha(criptografarSenha(usuario.getSenha())); 
	            return Optional.of(usuarioRepository.save(usuario)); 
	            }
	        
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
	        "Usuário não encontrado!", null); 
	        
	    }
		
		public Optional<UsuarioLogin> logarUsuario
		(Optional<UsuarioLogin> usuarioLogin){
			
			Optional<Usuario> usuario = usuarioRepository
					.findByUsuario(usuarioLogin.get().getUsuario()); 
			
			if(usuario.isPresent()) {
				if(compararSenhas(usuarioLogin.get().getSenha(),usuario.get().getSenha())){
					
					usuarioLogin.get().setId(usuario.get().getId());
					usuarioLogin.get().setNome(usuario.get().getNome());
					usuarioLogin.get().setFoto(usuario.get().getFoto());
					usuarioLogin.get().setToken(
					gerarBasicToken(usuarioLogin.get().getUsuario(),
					usuarioLogin.get().getSenha()));
					usuarioLogin.get().setSenha(usuario.get().getSenha());
					return usuarioLogin;

				}
				
			}
			
			throw new ResponseStatusException(
					HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos!", null);

		}


	private String criptografarSenha(String senha) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		String senhaEncoder = encoder.encode(senha);
		return senhaEncoder;
	}

	private boolean compararSenhas(String senhaDigitada, String senhaBanco) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		return encoder.matches(senhaDigitada, senhaBanco);
	}

	private String gerarBasicToken(String email, String password) {
		String estrutura = email + ":" + password;
		byte[] estruturaBase64 = Base64.encodeBase64(estrutura.getBytes(Charset.forName("US-ASCII")));
		return "Basic " + new String(estruturaBase64);
	}

	
	


	
	
}
