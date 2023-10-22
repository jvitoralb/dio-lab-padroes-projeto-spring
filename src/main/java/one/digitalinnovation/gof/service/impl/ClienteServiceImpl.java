package one.digitalinnovation.gof.service.impl;

import one.digitalinnovation.gof.model.Endereco;
import one.digitalinnovation.gof.model.EnderecoRepository;
import one.digitalinnovation.gof.service.ViaCepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import one.digitalinnovation.gof.model.Cliente;
import one.digitalinnovation.gof.model.ClienteRepository;
import one.digitalinnovation.gof.service.ClienteService;

/**
 * Implementação da <b>Strategy</b> {@link ClienteService}, a qual pode ser
 * injetada pelo Spring (via {@link Autowired}). Com isso, como essa classe é um
 * {@link Service}, ela será tratada como um <b>Singleton</b>.
 * 
 * @author falvojr
 */
@Service
public class ClienteServiceImpl implements ClienteService {
	// Singleton: Injetar os componentes do Spring com @Autowired.
	@Autowired
	private ClienteRepository clienteRepository;
	@Autowired
	private EnderecoRepository enderecoRepository;
	@Autowired
	private ViaCepService viaCepService;

	// Strategy: Implementar os métodos definidos na interface.
	// Facade: Abstrair integrações com subsistemas, provendo uma interface simples.

	@Override
	public Iterable<Cliente> buscarTodos() {
		// Buscar todos os Clientes.
		return clienteRepository.findAll();
	}

	@Override
	public Cliente buscarPorId(Long id) {
		// Buscar Cliente por ID.
		return clienteRepository.findById(id).orElse(null);
	}

	@Override
	public void inserir(Cliente cliente) {
		// Verificar se o endereço do Cliente já existe (pelo CEP).
		String cep = cliente.getEndereco().getCep();
		Endereco endereco = enderecoRepository.findById(cep).orElse(null);

		if (endereco == null) { // Caso não exista, integrar com o ViaCEP e persistir o retorno.
			endereco = viaCepService.consultarCep(cep);
			enderecoRepository.save(endereco);
		}

		cliente.setEndereco(endereco);

		// Inserir Cliente, vinculando o Endereço (novo ou existente).
		clienteRepository.save(cliente);
	}

	@Override
	public void atualizarEndereco(Long id, Cliente cliente) {
		// Buscar Cliente por ID, caso exista:
		Cliente alvo = clienteRepository.findById(id).orElse(null);
		boolean alvoEndereco = enderecoRepository.existsById(cliente.getEndereco().getCep());

		if (alvo == null || !alvoEndereco) {
			inserir(cliente);
		} else {
			// Alterar o Cliente, vinculando o Endereço (novo ou existente).
			alvo.setEndereco(cliente.getEndereco());
		}
	}

	@Override
	public void deletar(Long id) {
		// Deletar Cliente por ID.
		clienteRepository.deleteById(id);
	}
}
