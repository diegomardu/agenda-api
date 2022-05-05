package io.github.diegomardu.agendaapi.model.repository;

import io.github.diegomardu.agendaapi.model.entity.Contato;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContatoRepository extends JpaRepository<Contato, Integer> {
}
