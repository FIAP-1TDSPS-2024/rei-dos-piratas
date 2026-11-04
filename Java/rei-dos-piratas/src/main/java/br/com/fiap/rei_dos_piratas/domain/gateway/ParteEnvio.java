package br.com.fiap.rei_dos_piratas.domain.gateway;

/** Remetente ou destinatário do envio. {@code cpf} para pessoa física; {@code cnpj}, IE e CNAE para a loja. */
public record ParteEnvio(
        String nome,
        String email,
        String telefone,
        String cpf,
        String cnpj,
        String inscricaoEstadual,
        String cnae,
        String logradouro,
        String complemento,
        String numero,
        String bairro,
        String cidade,
        String cep,
        String uf
) {
    @Override
    public String toString() {
        // Sem dados pessoais (CPF, e-mail, telefone, endereço) em log
        return "ParteEnvio[nome=" + nome + ", cep=" + cep + "]";
    }
}
