export const obterMensagemErro = (erro, mensagemPadrao) => {
  const dados = erro?.response?.data;

  if (typeof dados === 'string' && dados.trim()) return dados;
  return dados?.message || dados?.erro || mensagemPadrao;
};
