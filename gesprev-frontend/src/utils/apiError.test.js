import { describe, expect, it } from 'vitest';
import { obterMensagemErro } from './apiError';

describe('obterMensagemErro', () => {
  it('prioriza uma mensagem textual retornada pela API', () => {
    const erro = { response: { data: 'Processo não encontrado' } };

    expect(obterMensagemErro(erro, 'Falha inesperada')).toBe('Processo não encontrado');
  });

  it('aceita os campos message e erro usados pelo backend', () => {
    expect(obterMensagemErro({ response: { data: { message: 'Documento inválido' } } }, 'Falha'))
      .toBe('Documento inválido');
    expect(obterMensagemErro({ response: { data: { erro: 'Acesso negado' } } }, 'Falha'))
      .toBe('Acesso negado');
  });

  it('usa a mensagem padrão quando a resposta não contém uma mensagem útil', () => {
    expect(obterMensagemErro(new Error('erro de rede'), 'Não foi possível concluir'))
      .toBe('Não foi possível concluir');
  });
});
