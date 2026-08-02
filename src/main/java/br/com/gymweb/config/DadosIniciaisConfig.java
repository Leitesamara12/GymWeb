package br.com.gymweb.config;

import br.com.gymweb.model.Aluno;
import br.com.gymweb.model.ExercicioCatalogo;
import br.com.gymweb.model.Instrutor;
import br.com.gymweb.repository.AlunoRepository;
import br.com.gymweb.repository.ExercicioCatalogoRepository;
import br.com.gymweb.repository.InstrutorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

/**
 * Cria dados mínimos somente quando ainda não existem registros.
 * As credenciais ficam documentadas no README e podem ser desativadas
 * com gymweb.seed.enabled=false.
 */
@Configuration
@ConditionalOnProperty(name = "gymweb.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DadosIniciaisConfig {

    @Bean
    @SuppressWarnings("null")
    CommandLineRunner carregarDadosIniciais(
            InstrutorRepository instrutorRepository,
            AlunoRepository alunoRepository,
            ExercicioCatalogoRepository exercicioCatalogoRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (instrutorRepository.findByCpf("00000000000").isEmpty()) {
                Instrutor dono = new Instrutor();
                dono.setNome("Administrador GymWeb");
                dono.setCpf("00000000000");
                dono.setSenha(passwordEncoder.encode("admin123"));
                dono.setCargo("DONO");
                dono.setEmail("admin@gymweb.local");
                dono.setTelefone("11999999999");
                dono.setEspecialidade("Gestão");
                instrutorRepository.save(dono);
            }

            if (alunoRepository.findByCpf("00000000001").isEmpty()) {
                Aluno aluno = new Aluno();
                aluno.setNome("Aluno Demonstração");
                aluno.setCpf("00000000001");
                aluno.setSenha(passwordEncoder.encode("aluno123"));
                aluno.setCargo("ALUNO");
                alunoRepository.save(aluno);
            }

            if (exercicioCatalogoRepository.count() == 0) {
                ExercicioCatalogo supino = new ExercicioCatalogo();
                supino.setNome("Supino reto");
                supino.setDescricao("Exercício para peitoral.");
                supino.setGrupoMuscular("Peitoral");

                ExercicioCatalogo agachamento = new ExercicioCatalogo();
                agachamento.setNome("Agachamento livre");
                agachamento.setDescricao("Exercício para membros inferiores.");
                agachamento.setGrupoMuscular("Pernas");

                ExercicioCatalogo remada = new ExercicioCatalogo();
                remada.setNome("Remada baixa");
                remada.setDescricao("Exercício para dorsais.");
                remada.setGrupoMuscular("Costas");

                exercicioCatalogoRepository.saveAll(List.of(supino, agachamento, remada));
            }
        };
    }
}
