package io.github.diegomardu.agendaapi.rest;

import io.github.diegomardu.agendaapi.model.entity.Contato;
import io.github.diegomardu.agendaapi.model.repository.ContatoRepository;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/contatos")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ContatoController {

    private final ContatoRepository repository;

    @GetMapping
    private Page<Contato> listarContatos(
            @RequestParam(value = "page", defaultValue = "0") Integer pagina,
            @RequestParam(value = "size", defaultValue = "10") Integer paginaTamanho
    ){
        Sort sort = Sort.by(Sort.Direction.ASC,"nome");
        PageRequest pageRequest = PageRequest.of(pagina,paginaTamanho, sort);
        return repository.findAll(pageRequest);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    private Contato salvar(@RequestBody Contato contato){
        return repository.save(contato);
    }

    @GetMapping("{id}")
    public Contato buscarPorId(@PathVariable Integer id){
        return repository
                .findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contato não existente"));
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletarContato(@PathVariable Integer id){
        repository
                .findById(id)
                .map(contato -> {
                    repository.delete(contato);
                    return Void.TYPE;
                })
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contato não existente"));
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizarContato(@PathVariable Integer id, @RequestBody Contato contatoAtualizado){
        repository
                .findById(id)
                .map(contato -> {
                    contatoAtualizado.setId(contato.getId());
                    return repository.save(contatoAtualizado);
                })
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Contato não existente"));
    }

    @PatchMapping("{id}/favorito")
    public void favorito(@PathVariable Integer id){
        Optional<Contato> contato = repository.findById(id);
        contato.ifPresent(c -> {
            boolean favorito = c.getFavorito() == Boolean.TRUE;
            c.setFavorito(!favorito);
            repository.save(c);
        });
    }

    @PutMapping("{id}/foto")
    public byte[] addFoto(@PathVariable Integer id,
                          @RequestParam("foto")Part arquivo){
        Optional<Contato> contato = repository.findById(id);
        return contato.map(c -> {
            try {
                InputStream is = arquivo.getInputStream();
                byte[] bytes = new byte[(int)arquivo.getSize()];
                IOUtils.readFully(is,bytes);
                c.setFoto(bytes);
                repository.save(c);
                is.close();
                return bytes;
            }catch (IOException e){
                return null;
            }
        }).orElse(null);
    }

}
