import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Layout from '../../components/Layout/Layout';
import {
  Box, Typography, Paper, Chip, Button, Divider,
  Table, TableBody, TableCell, TableContainer, TableHead,
  TableRow, IconButton, Alert, CircularProgress, Tabs, Tab,
  Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, MenuItem, Select, FormControl, InputLabel,
  Tooltip, LinearProgress, Checkbox, FormControlLabel
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import CloseIcon from '@mui/icons-material/Close';
import CalculateIcon from '@mui/icons-material/Calculate';
import UploadIcon from '@mui/icons-material/Upload';
import { buscarProcessoPorId } from '../../services/processoService';
import { listarChecklistPorProcesso } from '../../services/checklistService';
import { listarDocumentosPorProcesso, deletarDocumento, uploadDocumento } from '../../services/documentoService';
import { listarHistoricoPorProcesso } from '../../services/historicoService';
import { calcular, obterMemoriaPorProcesso } from '../../services/memoriaCalculoService';
import { useAuth } from '../../contexts/useAuth';
import { verificarFatoresExistentes, importarFatores } from '../../services/fatorAtualizacaoService';
import { gerarAto } from '../../services/atoService';
import { atualizarServidor, buscarServidorPorId } from '../../services/servidorService';
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';
import DownloadIcon from '@mui/icons-material/Download';
import AutoAwesomeIcon from '@mui/icons-material/AutoAwesome';
import FactCheckIcon from '@mui/icons-material/FactCheck';
import DataObjectIcon from '@mui/icons-material/DataObject';
import { downloadDocumento, processarDocumento, confirmarDados, atualizarJsonExtraido } from '../../services/documentoService';
import { obterMensagemErro } from '../../utils/apiError';
import AssistenteChat from '../../components/AssistenteChat/AssistenteChat';

const formatarMoeda = (valor) => new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL'
}).format(valor ?? 0);

const formatarPercentual = (valor) => new Intl.NumberFormat('pt-BR', {
  style: 'percent',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2
}).format(valor ?? 0);

const obterDataLocalISO = () => {
  const agora = new Date();
  const local = new Date(agora.getTime() - agora.getTimezoneOffset() * 60_000);
  return local.toISOString().split('T')[0];
};

const STATUS_CORES = {
  CADASTRADO: 'default',
  PENDENTE_DOCUMENTO: 'warning',
  EM_ANALISE: 'info',
  EM_CALCULO: 'primary',
  REJEITADO: 'error',
  FINALIZADO: 'success'
};

const STATUS_ROTULOS = {
  CADASTRADO: 'Cadastrado',
  PENDENTE_DOCUMENTO: 'Pendente de documentos',
  EM_ANALISE: 'Em análise',
  EM_CALCULO: 'Em cálculo',
  REJEITADO: 'Rejeitado',
  FINALIZADO: 'Finalizado'
};

const TIPOS_DOCUMENTO = [
  { valor: 'HOLERITE', rotulo: 'Holerite' },
  { valor: 'FICHA_FINANCEIRA', rotulo: 'Ficha Financeira' },
  { valor: 'CTS', rotulo: 'CTS' },
  { valor: 'FICHA_FUNCIONAL', rotulo: 'Ficha Funcional' }
];

const STATUS_VLM = {
  PENDENTE: { rotulo: 'Pendente', cor: 'warning' },
  PROCESSADO: { rotulo: 'Processado', cor: 'info' },
  ERRO: { rotulo: 'Erro', cor: 'error' },
  VALIDADO: { rotulo: 'Validado', cor: 'success' }
};

const formatarJson = (valor) => {
  try {
    const dados = typeof valor === 'string' ? JSON.parse(valor) : valor;
    return JSON.stringify(dados, null, 2);
  } catch {
    return valor;
  }
};

const parseJsonDocumento = (valor) => {
  try {
    return typeof valor === 'string' ? JSON.parse(valor) : (valor || {});
  } catch {
    return {};
  }
};

const numeroOuNull = (valor) => {
  if (valor === '' || valor === null || valor === undefined) return null;
  const normalizado = String(valor).replace(/\./g, '').replace(',', '.');
  const numero = Number(normalizado);
  return Number.isNaN(numero) ? null : numero;
};

const criarFormServidor = (servidor = {}) => ({
  nome: servidor.nome || '',
  cpf: servidor.cpf || '',
  pis: servidor.pis || '',
  sexo: servidor.sexo || 'MASCULINO',
  matricula: servidor.matricula || '',
  email: servidor.email || '',
  cargo: servidor.cargo || '',
  orgao: servidor.orgao || '',
  dtNascimento: servidor.dtNascimento || '',
  dtAdmissao: servidor.dtAdmissao || ''
});

export default function DetalheProcesso() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { usuario } = useAuth();
  const podeImportarFatores = usuario?.perfil === 'DIRETOR';
  const [aba, setAba] = useState(0);
  const [processo, setProcesso] = useState(null);
  const [servidor, setServidor] = useState(null);
  const [editandoServidor, setEditandoServidor] = useState(false);
  const [salvandoServidor, setSalvandoServidor] = useState(false);
  const [formServidor, setFormServidor] = useState(criarFormServidor());
  const [checklist, setChecklist] = useState([]);
  const [documentos, setDocumentos] = useState([]);
  const [memoria, setMemoria] = useState(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');
  const [calculando, setCalculando] = useState(false);
  const [dialogCalculo, setDialogCalculo] = useState(false);
  const [tipoCalculo, setTipoCalculo] = useState('INTEGRAL');
  const [historico, setHistorico] = useState([]);
  const [modalDocumento, setModalDocumento] = useState(false);
  const [salvandoDoc, setSalvandoDoc] = useState(false);
  const [documentoProcessando, setDocumentoProcessando] = useState(null);
  const [documentoValidando, setDocumentoValidando] = useState(null);
  const [documentoVisualizado, setDocumentoVisualizado] = useState(null);
  const [dadosDocumentoEditado, setDadosDocumentoEditado] = useState({});
  const [anoFolhasFinanceiras, setAnoFolhasFinanceiras] = useState('');
  const [salvandoJsonDocumento, setSalvandoJsonDocumento] = useState(false);
  const [formDoc, setFormDoc] = useState({
    tipoDocumento: 'HOLERITE',
    arquivo: null
  });
  const [fatoresExistem, setFatoresExistem] = useState(false);
  const [arquivoFator, setArquivoFator] = useState(null);
  const [portariaFator, setPortariaFator] = useState('');
  const [importando, setImportando] = useState(false);
  const [opcaoFator, setOpcaoFator] = useState('existente');
  const [modalAto, setModalAto] = useState(false);
  const [gerandoAto, setGerandoAto] = useState(false);
  const [formAto, setFormAto] = useState({
    naturezaAposentadoria: 'VOLUNTARIA',
    tipoCalculo: 'INTEGRAL',
    emendaConstitucional: 'EC_103',
    referenciaSalarial: '',
    dataFinalizacao: obterDataLocalISO()
  });

  const carregarDados = useCallback(async () => {
    try {
      const processoData = await buscarProcessoPorId(id);
      setProcesso(processoData);
      const servidorData = await buscarServidorPorId(processoData.servidorId);
      setServidor(servidorData);
      if (!editandoServidor) {
        setFormServidor(criarFormServidor(servidorData));
      }

      try {
      const fatores = await verificarFatoresExistentes();
      setFatoresExistem(fatores.existem);
        if (!fatores.existem) setOpcaoFator(podeImportarFatores ? 'importar' : 'existente');
      } catch {
        setFatoresExistem(false);
      }

      const [checklistData, documentosData, historicoData] = await Promise.all([
        listarChecklistPorProcesso(id),
        listarDocumentosPorProcesso(id),
        listarHistoricoPorProcesso(id)
      ]);
      setChecklist(checklistData);
      setDocumentos(documentosData);
      setHistorico(historicoData);

      try {
        const memoriaData = await obterMemoriaPorProcesso(id);
        setMemoria(memoriaData);
      } catch {
        // processo ainda não tem memória de cálculo
      }
    } catch {
      setErro('Erro ao carregar processo');
    } finally {
      setCarregando(false);
    }
  }, [id, podeImportarFatores, editandoServidor]);

  useEffect(() => {
    const carregarInicial = async () => {
      await carregarDados();
    };
    carregarInicial();
  }, [carregarDados]);

  const handleCalcular = async () => {
    setCalculando(true);
    try {
      await calcular(id, tipoCalculo);
      await carregarDados();
      setDialogCalculo(false);
    } catch (err) {
      setErro(obterMensagemErro(err, 'Erro ao gerar cálculo'));
    } finally {
      setCalculando(false);
    }
  };

  const iniciarEdicaoServidor = () => {
    setFormServidor(criarFormServidor(servidor));
    setEditandoServidor(true);
  };

  const cancelarEdicaoServidor = () => {
    setFormServidor(criarFormServidor(servidor));
    setEditandoServidor(false);
  };

  const atualizarCampoServidor = (campo, valor) => {
    setFormServidor(prev => ({ ...prev, [campo]: valor }));
  };

  const handleSalvarServidor = async () => {
    const camposObrigatorios = ['nome', 'cpf', 'sexo', 'matricula', 'cargo', 'orgao', 'dtNascimento', 'dtAdmissao'];
    const incompleto = camposObrigatorios.some(campo => !String(formServidor[campo] || '').trim());
    if (incompleto) {
      setErro('Preencha todos os campos obrigatórios do servidor antes de salvar.');
      return;
    }

    setSalvandoServidor(true);
    try {
      const atualizado = await atualizarServidor(servidor.id, {
        id: servidor.id,
        ...formServidor,
        email: formServidor.email || null
      });
      setServidor(atualizado);
      setFormServidor(criarFormServidor(atualizado));
      setEditandoServidor(false);
      await carregarDados();
    } catch (err) {
      setErro(obterMensagemErro(err, 'Erro ao atualizar dados do servidor'));
    } finally {
      setSalvandoServidor(false);
    }
  };

  const handleExcluirDocumento = async (docId) => {
    try {
      await deletarDocumento(docId);
      await carregarDados();
    } catch (err) {
      setErro(obterMensagemErro(err, 'Erro ao excluir documento'));
    }
  };

  const handleProcessarDocumento = async (docId) => {
    setDocumentoProcessando(docId);
    try {
      await processarDocumento(docId);
      await carregarDados();
    } catch (err) {
      setErro(obterMensagemErro(err, 'Erro ao processar documento'));
    } finally {
      setDocumentoProcessando(null);
    }
  };

  const handleValidarDocumento = async (docId) => {
    setDocumentoValidando(docId);
    try {
      await confirmarDados(docId);
      await carregarDados();
      return true;
    } catch (err) {
      setErro(obterMensagemErro(err, 'Erro ao validar documento'));
      return false;
    } finally {
      setDocumentoValidando(null);
    }
  };

  const abrirDadosExtraidos = (doc) => {
    const dados = parseJsonDocumento(doc.jsonExtraido);
    setDocumentoVisualizado(doc);
    setDadosDocumentoEditado(dados);
    if (doc.tipoDocumento === 'FICHA_FINANCEIRA') {
      const primeiroAno = (dados.folhas || [])
        .map((folha) => folha.anoReferencia || String(folha.competencia || '').split('/')[1])
        .filter(Boolean)
        .sort((a, b) => Number(a) - Number(b))[0];
      setAnoFolhasFinanceiras(primeiroAno ? String(primeiroAno) : '');
    } else {
      setAnoFolhasFinanceiras('');
    }
  };

  const handleSalvarJsonDocumento = async () => {
    if (!documentoVisualizado) return false;
    try {
      JSON.stringify(dadosDocumentoEditado);
    } catch {
      setErro('O JSON informado é inválido. Corrija a sintaxe antes de salvar.');
      return false;
    }

    setSalvandoJsonDocumento(true);
    try {
      const documentoAtualizado = await atualizarJsonExtraido(
        documentoVisualizado.id,
        JSON.stringify(dadosDocumentoEditado)
      );
      setDocumentoVisualizado(documentoAtualizado);
      setDadosDocumentoEditado(parseJsonDocumento(documentoAtualizado.jsonExtraido));
      await carregarDados();
      return true;
    } catch (err) {
      setErro(obterMensagemErro(err, 'Erro ao salvar correções do documento'));
      return false;
    } finally {
      setSalvandoJsonDocumento(false);
    }
  };

  const atualizarCampoDados = (campo, valor) => {
    setDadosDocumentoEditado(prev => ({ ...prev, [campo]: valor }));
  };

  const atualizarItemListaDados = (lista, indice, campo, valor) => {
    setDadosDocumentoEditado(prev => {
      const itens = [...(prev[lista] || [])];
      itens[indice] = { ...(itens[indice] || {}), [campo]: valor };
      return { ...prev, [lista]: itens };
    });
  };

  const adicionarItemListaDados = (lista, item) => {
    setDadosDocumentoEditado(prev => ({ ...prev, [lista]: [...(prev[lista] || []), item] }));
  };

  const removerItemListaDados = (lista, indice) => {
    setDadosDocumentoEditado(prev => ({
      ...prev,
      [lista]: (prev[lista] || []).filter((_, itemIndice) => itemIndice !== indice)
    }));
  };

  const renderFormularioDadosExtraidos = () => {
    const editavel = podeGerenciarDocumentos && documentoVisualizado?.statusVLM === 'PROCESSADO';
    const tipo = documentoVisualizado?.tipoDocumento;

    if (tipo === 'HOLERITE') {
      const proventos = dadosDocumentoEditado.proventos || [];
      return (
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' }, gap: 2 }}>
            <TextField label="Competência" value={dadosDocumentoEditado.mesReferencia || ''}
              disabled={!editavel} onChange={(e) => atualizarCampoDados('mesReferencia', e.target.value)} />
            <TextField label="Total de vencimentos" type="number" value={dadosDocumentoEditado.totalVencimentos ?? dadosDocumentoEditado.valorTotalProventos ?? ''}
              disabled={!editavel} onChange={(e) => atualizarCampoDados('totalVencimentos', numeroOuNull(e.target.value))} />
          </Box>
          <Divider />
          <Typography fontWeight="bold">Proventos</Typography>
          {proventos.map((provento, indice) => (
            <Paper key={indice} variant="outlined" sx={{ p: 2 }}>
              <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '2fr 1fr 1fr auto' }, gap: 2, alignItems: 'center' }}>
                <TextField label="Descrição" value={provento.descricao || ''} disabled={!editavel}
                  onChange={(e) => atualizarItemListaDados('proventos', indice, 'descricao', e.target.value)} />
                <TextField label="Referência" value={provento.referencia || ''} disabled={!editavel}
                  onChange={(e) => atualizarItemListaDados('proventos', indice, 'referencia', e.target.value)} />
                <TextField label="Valor" type="number" value={provento.valor ?? ''} disabled={!editavel}
                  onChange={(e) => atualizarItemListaDados('proventos', indice, 'valor', numeroOuNull(e.target.value))} />
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <FormControlLabel control={<Checkbox checked={Boolean(provento.vencimento)} disabled={!editavel}
                    onChange={(e) => atualizarItemListaDados('proventos', indice, 'vencimento', e.target.checked)} />} label="Vencimento" />
                  {editavel && <Button color="error" onClick={() => removerItemListaDados('proventos', indice)}>Remover</Button>}
                </Box>
              </Box>
            </Paper>
          ))}
          {editavel && (
            <Button variant="outlined" onClick={() => adicionarItemListaDados('proventos', { id: null, descricao: '', referencia: '', valor: 0, vencimento: true })}>
              Adicionar provento
            </Button>
          )}
        </Box>
      );
    }

    if (tipo === 'FICHA_FINANCEIRA') {
      const folhas = dadosDocumentoEditado.folhas || [];
      const obterAnoFolha = (folha) => String(folha.anoReferencia || String(folha.competencia || '').split('/')[1] || 'Sem ano');
      const anos = [...new Set(folhas.map(obterAnoFolha))]
        .sort((a, b) => Number(a) - Number(b));
      const anoSelecionado = anoFolhasFinanceiras || anos[0] || '';
      const folhasVisiveis = folhas
        .map((folha, indice) => ({ folha, indice }))
        .filter(({ folha }) => obterAnoFolha(folha) === anoSelecionado);

      return (
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
          <Alert severity="warning">
            A ficha financeira pode ter vários meses, vários anos e competências repetidas. Cada linha abaixo representa uma folha/bloco usado no cálculo proporcional pelo valor líquido.
          </Alert>
          <Box>
            <Typography variant="body2" color="text.secondary">
              {folhas.length} folhas no total. Selecione um ano para conferir as compet?ncias.
            </Typography>
            <Tabs
              value={anoSelecionado}
              onChange={(_, novoAno) => setAnoFolhasFinanceiras(novoAno)}
              variant="scrollable"
              scrollButtons="auto"
              sx={{ mt: 1, borderBottom: 1, borderColor: 'divider' }}
            >
              {anos.map((ano) => (
                <Tab
                  key={ano}
                  value={ano}
                  label={`${ano} (${folhas.filter((folha) => obterAnoFolha(folha) === ano).length})`}
                />
              ))}
            </Tabs>
          </Box>
          {folhasVisiveis.map(({ folha, indice }) => {
            return (
            <Paper key={indice} variant="outlined" sx={{ p: 2 }}>
              <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '1fr 1fr 1fr 1fr 1fr auto' }, gap: 2, alignItems: 'center' }}>
                <TextField label="Ano" type="number" value={folha.anoReferencia ?? dadosDocumentoEditado.anoReferencia ?? ''} disabled={!editavel}
                  onChange={(e) => atualizarItemListaDados('folhas', indice, 'anoReferencia', numeroOuNull(e.target.value))} />
                <TextField label="Competência" placeholder="MM/yyyy" value={folha.competencia || ''} disabled={!editavel}
                  onChange={(e) => atualizarItemListaDados('folhas', indice, 'competencia', e.target.value)} />
                <TextField label="Vencimentos" type="number" value={folha.vencimentos ?? ''} disabled={!editavel}
                  onChange={(e) => atualizarItemListaDados('folhas', indice, 'vencimentos', numeroOuNull(e.target.value))} />
                <TextField label="Descontos" type="number" value={folha.descontos ?? ''} disabled={!editavel}
                  onChange={(e) => atualizarItemListaDados('folhas', indice, 'descontos', numeroOuNull(e.target.value))} />
                <TextField label="Líquido" type="number" required value={folha.liquido ?? ''} disabled={!editavel}
                  onChange={(e) => atualizarItemListaDados('folhas', indice, 'liquido', numeroOuNull(e.target.value))} />
                {editavel && <Button color="error" onClick={() => removerItemListaDados('folhas', indice)}>Remover</Button>}
              </Box>
            </Paper>
            );
          })}
          {editavel && (
            <Button variant="outlined" onClick={() => adicionarItemListaDados('folhas', { anoReferencia: new Date().getFullYear(), competencia: '', vencimentos: 0, descontos: 0, liquido: 0 })}>
              Adicionar folha/competência
            </Button>
          )}
        </Box>
      );
    }

    if (tipo === 'CTS') {
      return (
        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' }, gap: 2, mt: 1 }}>
          <TextField label="Início da contribuição" placeholder="dd/MM/yyyy" value={dadosDocumentoEditado.inicioContribuicao || ''} disabled={!editavel}
            onChange={(e) => atualizarCampoDados('inicioContribuicao', e.target.value)} />
          <TextField label="Fim da contribuição" placeholder="dd/MM/yyyy" value={dadosDocumentoEditado.fimContribuicao || ''} disabled={!editavel}
            onChange={(e) => atualizarCampoDados('fimContribuicao', e.target.value)} />
          <TextField label="Tempo de averbação" type="number" value={dadosDocumentoEditado.tempoAverbacao ?? ''} disabled={!editavel}
            onChange={(e) => atualizarCampoDados('tempoAverbacao', numeroOuNull(e.target.value))} />
          <TextField label="Total bruto" type="number" value={dadosDocumentoEditado.totalBruto ?? ''} disabled={!editavel}
            onChange={(e) => atualizarCampoDados('totalBruto', numeroOuNull(e.target.value))} />
          <TextField label="Faltas" type="number" value={dadosDocumentoEditado.faltas ?? ''} disabled={!editavel}
            onChange={(e) => atualizarCampoDados('faltas', numeroOuNull(e.target.value))} />
          <TextField label="Total de dias" type="number" value={dadosDocumentoEditado.totalDias ?? ''} disabled={!editavel}
            onChange={(e) => atualizarCampoDados('totalDias', numeroOuNull(e.target.value))} />
          <TextField label="Tempo legível" value={dadosDocumentoEditado.tempoLegivel || ''} disabled={!editavel}
            onChange={(e) => atualizarCampoDados('tempoLegivel', e.target.value)} sx={{ gridColumn: { md: '1 / -1' } }} />
        </Box>
      );
    }

    if (tipo === 'FICHA_FUNCIONAL') {
      return (
        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' }, gap: 2, mt: 1 }}>
          {['matricula', 'nome', 'cpf', 'pis', 'email', 'cargo', 'orgao'].map(campo => (
            <TextField key={campo} label={campo} value={dadosDocumentoEditado[campo] || ''} disabled={!editavel}
              onChange={(e) => atualizarCampoDados(campo, e.target.value)} />
          ))}
          <TextField label="Data de nascimento" placeholder="yyyy-MM-dd" value={dadosDocumentoEditado.dtNascimento || ''} disabled={!editavel}
            onChange={(e) => atualizarCampoDados('dtNascimento', e.target.value)} />
          <TextField label="Data de admissão" placeholder="yyyy-MM-dd" value={dadosDocumentoEditado.dtAdmissao || ''} disabled={!editavel}
            onChange={(e) => atualizarCampoDados('dtAdmissao', e.target.value)} />
          <FormControl fullWidth disabled={!editavel}>
            <InputLabel>Sexo</InputLabel>
            <Select value={dadosDocumentoEditado.sexo || ''} label="Sexo" onChange={(e) => atualizarCampoDados('sexo', e.target.value)}>
              <MenuItem value="">Não informado</MenuItem>
              <MenuItem value="FEMININO">Feminino</MenuItem>
              <MenuItem value="MASCULINO">Masculino</MenuItem>
            </Select>
          </FormControl>
        </Box>
      );
    }

    return (
      <TextField
        label="Dados extraídos"
        value={formatarJson(dadosDocumentoEditado)}
        multiline
        minRows={12}
        fullWidth
        disabled
      />
    );
  };

  const abrirModalDocumento = () => {
    const primeiroTipoDisponivel = TIPOS_DOCUMENTO.find(
      tipo => !documentos.some(doc => doc.tipoDocumento === tipo.valor)
    );
    if (!primeiroTipoDisponivel) return;
    setFormDoc({ tipoDocumento: primeiroTipoDisponivel.valor, arquivo: null });
    setModalDocumento(true);
  };

  const handleGerarAto = async () => {
    setGerandoAto(true);
    try {
      const blob = await gerarAto({
        ...formAto,
        processoId: parseInt(id)
      });

      // Abre o PDF no navegador
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `ato_aposentadoria_processo_${processo.numeroProcesso}.pdf`;
      link.click();
      window.URL.revokeObjectURL(url);

      setModalAto(false);
      await carregarDados();
    } catch (err) {
      setErro(obterMensagemErro(err, 'Erro ao gerar Ato de Aposentadoria'));
    } finally {
      setGerandoAto(false);
    }
  };

  const podeGerenciarDocumentos = ['CADASTRADO', 'PENDENTE_DOCUMENTO'].includes(processo?.status);
  const itensChecklistValidos = checklist.filter(item => item.entregue && item.valido).length;
  const progressoChecklist = checklist.length ? (itensChecklistValidos / checklist.length) * 100 : 0;
  const precisaReprocessarAposReabertura = Boolean(
    processo?.status === 'PENDENTE_DOCUMENTO' &&
    checklist.length > 0 &&
    checklist.every(item => item.entregue && item.valido)
  );
  const formularioAtoValido = Boolean(
    formAto.referenciaSalarial.trim() &&
    formAto.dataFinalizacao
  );

  if (carregando) return <Layout><Box sx={{ display: 'flex', justifyContent: 'center', mt: 5 }}><CircularProgress /></Box></Layout>;

  return (
    <Layout>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 3 }}>
        <IconButton onClick={() => navigate('/processos')}><ArrowBackIcon /></IconButton>
        <Typography variant="h5" fontWeight="bold">
          Processo Nº {processo?.numeroProcesso}
        </Typography>
        <Chip
          label={STATUS_ROTULOS[processo?.status] || processo?.status}
          color={STATUS_CORES[processo?.status] || 'default'}
        />
      </Box>

      {erro && <Alert severity="error" closeText="Fechar" sx={{ mb: 2 }} onClose={() => setErro('')}>{erro}</Alert>}

      <Tabs value={aba} onChange={(_, v) => setAba(v)} sx={{ mb: 3 }}>
        <Tab label="Servidor" />
        <Tab label="Documentos" />
        <Tab label="Memória de Cálculo" />
        <Tab label="Histórico" />
        <Tab label="Assistente" />
      </Tabs>

      {/* ABA 0: SERVIDOR */}
      {aba === 0 && (
        <Paper sx={{ p: 3 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 2, alignItems: 'center', mb: 2 }}>
            <Typography variant="h6" fontWeight="bold">Dados do Servidor</Typography>
            {servidor && !editandoServidor && (
              <Button variant="outlined" onClick={iniciarEdicaoServidor}>
                Editar dados
              </Button>
            )}
          </Box>
          <Divider sx={{ mb: 3 }} />
          {servidor ? (
            editandoServidor ? (
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
                <Alert severity="info">
                  Corrija aqui os dados cadastrais usados no processo, na memria de clculo, no ato de aposentadoria e no assistente.
                </Alert>

                <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: 'repeat(2, 1fr)' }, gap: 2 }}>
                  <TextField label="Nome" required fullWidth value={formServidor.nome} onChange={(e) => atualizarCampoServidor('nome', e.target.value)} />
                  <TextField label="CPF" required fullWidth value={formServidor.cpf} onChange={(e) => atualizarCampoServidor('cpf', e.target.value)} />
                  <TextField label="PIS/PASEP/NIT" fullWidth value={formServidor.pis} onChange={(e) => atualizarCampoServidor('pis', e.target.value)} />
                  <TextField label="Sexo" required select fullWidth value={formServidor.sexo} onChange={(e) => atualizarCampoServidor('sexo', e.target.value)}>
                    <MenuItem value="MASCULINO">Masculino</MenuItem>
                    <MenuItem value="FEMININO">Feminino</MenuItem>
                  </TextField>
                  <TextField label="Matrícula" required fullWidth value={formServidor.matricula} onChange={(e) => atualizarCampoServidor('matricula', e.target.value)} />
                  <TextField label="E-mail" type="email" fullWidth value={formServidor.email} onChange={(e) => atualizarCampoServidor('email', e.target.value)} />
                  <TextField label="Cargo" required fullWidth value={formServidor.cargo} onChange={(e) => atualizarCampoServidor('cargo', e.target.value)} />
                  <TextField label="Órgão" required fullWidth value={formServidor.orgao} onChange={(e) => atualizarCampoServidor('orgao', e.target.value)} />
                  <TextField label="Data de nascimento" required type="date" fullWidth value={formServidor.dtNascimento} onChange={(e) => atualizarCampoServidor('dtNascimento', e.target.value)} InputLabelProps={{ shrink: true }} />
                  <TextField label="Data de admissão" required type="date" fullWidth value={formServidor.dtAdmissao} onChange={(e) => atualizarCampoServidor('dtAdmissao', e.target.value)} InputLabelProps={{ shrink: true }} />
                </Box>

                <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2 }}>
                  <Button onClick={cancelarEdicaoServidor} disabled={salvandoServidor}>Cancelar</Button>
                  <Button variant="contained" onClick={handleSalvarServidor} disabled={salvandoServidor}>
                    {salvandoServidor ? <CircularProgress size={22} /> : 'Salvar altera??es'}
                  </Button>
                </Box>
              </Box>
            ) : (
              <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: 'repeat(2, 1fr)' }, gap: 2 }}>
                <Typography><strong>Nome:</strong> {servidor.nome || '-'}</Typography>
                <Typography><strong>CPF:</strong> {servidor.cpf || '-'}</Typography>
                <Typography><strong>PIS/PASEP/NIT:</strong> {servidor.pis || '-'}</Typography>
                <Typography><strong>Sexo:</strong> {servidor.sexo === 'FEMININO' ? 'Feminino' : servidor.sexo === 'MASCULINO' ? 'Masculino' : '-'}</Typography>
                <Typography><strong>Matrícula:</strong> {servidor.matricula || '-'}</Typography>
                <Typography><strong>E-mail:</strong> {servidor.email || '-'}</Typography>
                <Typography><strong>Cargo:</strong> {servidor.cargo || '-'}</Typography>
                <Typography><strong>Órgão:</strong> {servidor.orgao || '-'}</Typography>
                <Typography><strong>Data de nascimento:</strong> {servidor.dtNascimento ? new Date(`${servidor.dtNascimento}T00:00:00`).toLocaleDateString('pt-BR') : '-'}</Typography>
                <Typography><strong>Data de admissão:</strong> {servidor.dtAdmissao ? new Date(`${servidor.dtAdmissao}T00:00:00`).toLocaleDateString('pt-BR') : '-'}</Typography>
              </Box>
            )
          ) : (
            <Alert severity="warning">Dados do servidor no encontrados.</Alert>
          )}
        </Paper>
      )}
      {/* ABA 1: DOCUMENTOS E CHECKLIST FUNDIDOS */}
      {aba === 1 && (
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
          
          {/* Sessão Checklist */}
          <Box>
            <Typography variant="h6" fontWeight="bold" mb={2}>Checklist de Validação</Typography>
            <Box sx={{ mb: 2 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                <Typography variant="body2" color="text.secondary">Progresso documental</Typography>
                <Typography variant="body2" fontWeight="bold">
                  {itensChecklistValidos} de {checklist.length} validados
                </Typography>
              </Box>
              <LinearProgress variant="determinate" value={progressoChecklist} />
            </Box>
            {precisaReprocessarAposReabertura && (
              <Alert severity="info" sx={{ mb: 2 }}>
                Processo reaberto: reprocesse e valide ao menos um documento para submetê-lo novamente à análise.
              </Alert>
            )}
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Tipo de Documento</TableCell>
                    <TableCell align="center">Entregue</TableCell>
                    <TableCell align="center">Válido</TableCell>
                    <TableCell>Observação</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {checklist.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={4} align="center">Nenhum item no checklist</TableCell>
                    </TableRow>
                  ) : checklist.map((item) => (
                    <TableRow key={item.id}>
                      <TableCell>{item.tipoDocumento}</TableCell>
                      <TableCell align="center">
                        <Chip
                          label={item.entregue ? 'Entregue' : 'Pendente'}
                          color={item.entregue ? 'success' : 'warning'}
                          size="small"
                        />
                      </TableCell>
                      <TableCell align="center">
                        <Chip
                          label={item.valido ? 'Válido' : 'Não validado'}
                          color={item.valido ? 'success' : 'default'}
                          size="small"
                        />
                      </TableCell>
                      <TableCell>{item.observacao}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Box>

          <Divider />

          {/* Sessão Arquivos Anexados */}
          <Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6" fontWeight="bold">Arquivos Anexados</Typography>
              <Box sx={{ display: 'flex', gap: 1 }}>
                {podeGerenciarDocumentos && documentos.length < TIPOS_DOCUMENTO.length && (
                  <Button variant="contained" startIcon={<UploadIcon />} onClick={abrirModalDocumento}>
                    Anexar Documento
                  </Button>
                )}
              </Box>
            </Box>
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Nome Original</TableCell>
                    <TableCell>Tipo</TableCell>
                    <TableCell>Status VLM</TableCell>
                    <TableCell>Upload</TableCell>
                    <TableCell align="center">Ações</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {documentos.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={5} align="center">Nenhum documento anexado</TableCell>
                    </TableRow>
                  ) : documentos.map((doc) => (
                    <TableRow key={doc.id}>
                      <TableCell>{doc.nomeOriginal}</TableCell>
                      <TableCell>{doc.tipoDocumento}</TableCell>
                      <TableCell>
                        <Chip
                          label={STATUS_VLM[doc.statusVLM]?.rotulo || doc.statusVLM}
                          color={STATUS_VLM[doc.statusVLM]?.cor || 'default'}
                          size="small"
                        />
                      </TableCell>
                      <TableCell>{doc.dtUpload ? new Date(doc.dtUpload).toLocaleDateString('pt-BR') : '-'}</TableCell>
                      <TableCell align="center">
                        {podeGerenciarDocumentos && (
                          ['PENDENTE', 'ERRO'].includes(doc.statusVLM) ||
                          (precisaReprocessarAposReabertura && doc.statusVLM === 'VALIDADO')
                        ) && (
                          <Tooltip title={doc.statusVLM === 'PENDENTE' ? 'Processar com IA' : 'Processar novamente'}>
                            <span>
                              <IconButton
                                color="primary"
                                disabled={documentoProcessando === doc.id}
                                onClick={() => handleProcessarDocumento(doc.id)}
                              >
                                {documentoProcessando === doc.id
                                  ? <CircularProgress size={20} />
                                  : <AutoAwesomeIcon />}
                              </IconButton>
                            </span>
                          </Tooltip>
                        )}
                        {doc.jsonExtraido && (
                          <Tooltip title="Visualizar dados extraídos">
                            <IconButton onClick={() => abrirDadosExtraidos(doc)}>
                              <DataObjectIcon />
                            </IconButton>
                          </Tooltip>
                        )}
                        {podeGerenciarDocumentos && doc.statusVLM === 'PROCESSADO' && (
                          <Tooltip title="Revisar e validar dados">
                            <span>
                              <IconButton
                                color="success"
                                disabled={documentoValidando === doc.id}
                                onClick={() => abrirDadosExtraidos(doc)}
                              >
                                {documentoValidando === doc.id
                                  ? <CircularProgress size={20} />
                                  : <FactCheckIcon />}
                              </IconButton>
                            </span>
                          </Tooltip>
                        )}
                        <Tooltip title="Download">
                          <IconButton onClick={() => downloadDocumento(doc.id, doc.nomeOriginal)}>
                            <DownloadIcon />
                          </IconButton>
                        </Tooltip>
                        {usuario?.perfil === 'DIRETOR' && podeGerenciarDocumentos && (
                          <Tooltip title="Excluir">
                            <IconButton color="error" onClick={() => handleExcluirDocumento(doc.id)}>
                              <CloseIcon />
                            </IconButton>
                          </Tooltip>
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Box>

          {/* Modal Anexar Documento */}
          <Dialog open={modalDocumento} onClose={() => setModalDocumento(false)} maxWidth="sm" fullWidth>
            <DialogTitle>Anexar Documento</DialogTitle>
            <DialogContent>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
                <FormControl fullWidth>
                  <InputLabel>Tipo de Documento</InputLabel>
                  <Select
                    value={formDoc.tipoDocumento}
                    label="Tipo de Documento"
                    onChange={(e) => setFormDoc({ ...formDoc, tipoDocumento: e.target.value })}
                  >
                    {TIPOS_DOCUMENTO.map(tipo => (
                      <MenuItem
                        key={tipo.valor}
                        value={tipo.valor}
                        disabled={documentos.some(doc => doc.tipoDocumento === tipo.valor)}
                      >
                        {tipo.rotulo}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>

                <Button variant="outlined" component="label" fullWidth>
                  {formDoc.arquivo ? formDoc.arquivo.name : 'Selecionar arquivo PDF'}
                  <input
                    type="file"
                    accept=".pdf"
                    hidden
                    onChange={(e) => setFormDoc({ ...formDoc, arquivo: e.target.files[0] })}
                  />
                </Button>

                {formDoc.arquivo && (
                  <Typography variant="body2" color="text.secondary">
                    Arquivo selecionado: {formDoc.arquivo.name}
                  </Typography>
                )}
              </Box>
            </DialogContent>
            <DialogActions>
              <Button onClick={() => setModalDocumento(false)}>Cancelar</Button>
              <Button
                variant="contained"
                disabled={salvandoDoc || !formDoc.arquivo}
                onClick={async () => {
                  setSalvandoDoc(true);
                  try {
                    await uploadDocumento(parseInt(id), formDoc.arquivo, formDoc.tipoDocumento);
                    await carregarDados();
                    setModalDocumento(false);
                    setFormDoc({ tipoDocumento: 'HOLERITE', arquivo: null });
                  } catch (err) {
                    setErro(obterMensagemErro(err, 'Erro ao anexar documento'));
                  } finally {
                    setSalvandoDoc(false);
                  }
                }}
              >
                {salvandoDoc ? <CircularProgress size={24} /> : 'Anexar'}
              </Button>
            </DialogActions>
          </Dialog>
        </Box>
      )}

      <Dialog
        open={Boolean(documentoVisualizado)}
        onClose={() => {
          setDocumentoVisualizado(null);
          setDadosDocumentoEditado({});
          setAnoFolhasFinanceiras('');
        }}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>Dados extraídos  {documentoVisualizado?.nomeOriginal}</DialogTitle>
        <DialogContent>
          {podeGerenciarDocumentos && documentoVisualizado?.statusVLM === 'PROCESSADO' && (
            <Alert severity="info" sx={{ mb: 2 }}>
              Revise os dados extra­dos pela IA. Se algum campo estiver ausente ou incorreto,
              corrija o JSON abaixo antes de confirmar a valida§£o.
            </Alert>
          )}
          {renderFormularioDadosExtraidos()}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => {
            setDocumentoVisualizado(null);
            setDadosDocumentoEditado({});
            setAnoFolhasFinanceiras('');
          }}>Fechar</Button>
          {podeGerenciarDocumentos && documentoVisualizado?.statusVLM === 'PROCESSADO' && (
            <Button
              variant="outlined"
              disabled={salvandoJsonDocumento}
              onClick={handleSalvarJsonDocumento}
            >
              {salvandoJsonDocumento ? <CircularProgress size={24} /> : 'Salvar correções'}
            </Button>
          )}
          {podeGerenciarDocumentos && documentoVisualizado?.statusVLM === 'PROCESSADO' && (
            <Button
              variant="contained"
              color="success"
              startIcon={documentoValidando === documentoVisualizado.id ? null : <FactCheckIcon />}
              disabled={documentoValidando === documentoVisualizado.id || salvandoJsonDocumento}
              onClick={async () => {
                const salvo = await handleSalvarJsonDocumento();
                if (!salvo) return;
                const validado = await handleValidarDocumento(documentoVisualizado.id);
                if (validado) {
                  setDocumentoVisualizado(null);
                  setDadosDocumentoEditado({});
                  setAnoFolhasFinanceiras('');
                }
              }}
            >
              {documentoValidando === documentoVisualizado.id
                ? <CircularProgress size={24} />
                : 'Confirmar e validar'}
            </Button>
          )}
        </DialogActions>
      </Dialog>

      {/* Modal Gerar Ato de Aposentadoria */}
      <Dialog open={modalAto} onClose={() => setModalAto(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{processo?.status === 'FINALIZADO' ? 'Baixar Ato de Aposentadoria' : 'Gerar Ato de Aposentadoria'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>

            <FormControl fullWidth>
              <InputLabel>Natureza da Aposentadoria</InputLabel>
              <Select
                value={formAto.naturezaAposentadoria}
                label="Natureza da Aposentadoria"
                onChange={(e) => setFormAto({ ...formAto, naturezaAposentadoria: e.target.value })}
              >
                <MenuItem value="VOLUNTARIA">Voluntária</MenuItem>
                <MenuItem value="COMPULSORIA">Compulsória</MenuItem>
                <MenuItem value="INVALIDEZ">Invalidez</MenuItem>
              </Select>
            </FormControl>

            <FormControl fullWidth>
              <InputLabel>Emenda Constitucional</InputLabel>
              <Select
                value={formAto.emendaConstitucional}
                label="Emenda Constitucional"
                onChange={(e) => setFormAto({ ...formAto, emendaConstitucional: e.target.value })}
              >
                <MenuItem value="EC_41">EC 41/2003</MenuItem>
                <MenuItem value="EC_47">EC 47/2005</MenuItem>
                <MenuItem value="EC_103">EC 103/2019</MenuItem>
              </Select>
            </FormControl>

            <FormControl fullWidth>
              <InputLabel>Tipo de Cálculo</InputLabel>
              <Select
                value={formAto.tipoCalculo}
                label="Tipo de Cálculo"
                disabled
              >
                <MenuItem value="INTEGRAL">Integral</MenuItem>
                <MenuItem value="PROPORCIONAL">Proporcional</MenuItem>
              </Select>
            </FormControl>

            <TextField
              label="Referência Salarial"
              required
              fullWidth
              placeholder="Ex: PROF IV / D / 11"
              value={formAto.referenciaSalarial}
              onChange={(e) => setFormAto({ ...formAto, referenciaSalarial: e.target.value })}
            />

            <Alert severity="info">
              As parcelas do ato serão preenchidas automaticamente a partir da memória de cálculo.
            </Alert>

            <TextField
              label="Data de Finalização"
              required
              type="date"
              fullWidth
              slotProps={{ inputLabel: { shrink: true } }}
              value={formAto.dataFinalizacao}
              onChange={(e) => setFormAto({ ...formAto, dataFinalizacao: e.target.value })}
            />

          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setModalAto(false)}>Cancelar</Button>
          <Button
            variant="contained"
            color="success"
            onClick={handleGerarAto}
            disabled={gerandoAto || !formularioAtoValido}
          >
            {gerandoAto ? <CircularProgress size={24} /> : processo?.status === 'FINALIZADO' ? 'Baixar PDF' : 'Gerar PDF'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* ABA 2: MEMRIA DE CÁLCULO */}
      {aba === 2 && (
        <Box>
          {memoria ? (
            <Paper sx={{ p: 3 }}>
              <Typography variant="h6" fontWeight="bold" mb={2}>Resultado do Cálculo</Typography>
              <Divider sx={{ mb: 2 }} />
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                <Typography><strong>Tipo:</strong> {memoria.tipoCalculo}</Typography>
                {memoria.tipoCalculo === 'INTEGRAL' ? (
                  <>
                    <Typography><strong>Origem do cálculo:</strong> último holerite validado</Typography>
                    <Typography><strong>Competência do holerite:</strong> {memoria.holeriteMesReferencia || 'Não informada'}</Typography>
                    <Typography><strong>Total dos vencimentos:</strong> {formatarMoeda(memoria.holeriteValorTotalProventos)}</Typography>
                    <Typography variant="subtitle1" fontWeight="bold" sx={{ mt: 2 }}>Vencimentos considerados</Typography>
                    <TableContainer variant="outlined" component={Paper}>
                      <Table size="small">
                        <TableHead>
                          <TableRow>
                            <TableCell>Descrição</TableCell>
                            <TableCell>Referência</TableCell>
                            <TableCell align="right">Valor</TableCell>
                          </TableRow>
                        </TableHead>
                        <TableBody>
                          {(memoria.vencimentosHolerite || []).map((provento) => (
                            <TableRow key={provento.id || `${provento.descricao}-${provento.valor}`}>
                              <TableCell>{provento.descricao}</TableCell>
                              <TableCell>{provento.referencia}</TableCell>
                              <TableCell align="right">{formatarMoeda(provento.valor)}</TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </TableContainer>
                  </>
                ) : (
                  <>
                    <Typography><strong>Média Aritmética:</strong> {formatarMoeda(memoria.mediaAritmetica)}</Typography>
                    <Typography variant="subtitle1" fontWeight="bold" sx={{ mt: 2 }}>Competências da ficha financeira</Typography>
                    <TableContainer variant="outlined" component={Paper}>
                      <Table size="small">
                        <TableHead>
                          <TableRow>
                            <TableCell>Competência</TableCell>
                            <TableCell align="right">Líquido original</TableCell>
                            <TableCell align="right">Fator</TableCell>
                            <TableCell align="right">Valor corrigido</TableCell>
                            <TableCell align="center">Na média</TableCell>
                          </TableRow>
                        </TableHead>
                        <TableBody>
                          {(memoria.folhasCalculo || []).map((folha, indice) => (
                            <TableRow key={`${folha.anoReferencia}-${folha.competencia}-${indice}`}>
                              <TableCell>{folha.competencia?.includes('/') ? folha.competencia : `${folha.competencia}/${folha.anoReferencia}`}</TableCell>
                              <TableCell align="right">{formatarMoeda(folha.valorOriginal)}</TableCell>
                              <TableCell align="right">{folha.fatorAtualizacao}</TableCell>
                              <TableCell align="right">{formatarMoeda(folha.valorCorrigido)}</TableCell>
                              <TableCell align="center">
                                <Chip size="small" color={folha.utilizadaNaMedia ? 'success' : 'default'} label={folha.utilizadaNaMedia ? 'Sim' : 'Não'} />
                              </TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </TableContainer>
                  </>
                )}
                <Typography><strong>Valor do Benefício:</strong> {formatarMoeda(memoria.valorBeneficio)}</Typography>
                {memoria.tipoCalculo === 'PROPORCIONAL' && (
                  <Typography><strong>Proporcionalidade:</strong> {formatarPercentual(memoria.proporcionalidade)}</Typography>
                )}
              </Box>
              {processo?.status === 'EM_ANALISE' && (
                <Button
                  variant="contained"
                  startIcon={<CalculateIcon />}
                  onClick={() => setDialogCalculo(true)}
                  sx={{ mt: 2 }}
                >
                  Recalcular
                </Button>
              )}
              {usuario?.perfil === 'DIRETOR' && ['EM_CALCULO', 'FINALIZADO'].includes(processo?.status) && (
                <Button
                  variant="contained"
                  color="success"
                  startIcon={<PictureAsPdfIcon />}
                  onClick={() => {
                    setFormAto(prev => ({
                      ...prev,
                      tipoCalculo: memoria.tipoCalculo,
                      referenciaSalarial: prev.referenciaSalarial || 'Conforme memória de cálculo'
                    }));
                    setModalAto(true);
                  }}
                  sx={{ mt: 2 }}
                >
                  {processo?.status === 'FINALIZADO' ? 'Baixar Ato de Aposentadoria' : 'Gerar Ato de Aposentadoria'}
                </Button>
              )}
            </Paper>
          ) : (
            <Paper sx={{ p: 3, textAlign: 'center' }}>
              <Typography color="text.secondary" mb={2}>Nenhum cálculo gerado ainda</Typography>
              {processo?.status === 'EM_ANALISE' && (
                <Button
                  variant="contained"
                  startIcon={<CalculateIcon />}
                  onClick={() => setDialogCalculo(true)}
                >
                  Gerar Cálculo
                </Button>
              )}
            </Paper>
          )}
        </Box>
      )}

      {/* Dialog Tipo de Cálculo */}
      <Dialog open={dialogCalculo} onClose={() => setDialogCalculo(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Gerar Memória de Cálculo</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>

            <FormControl fullWidth>
              <InputLabel>Tipo de Cálculo</InputLabel>
              <Select
                value={tipoCalculo}
                label="Tipo de Cálculo"
                onChange={(e) => {
                  setTipoCalculo(e.target.value);
                  if (e.target.value === 'INTEGRAL') setOpcaoFator('existente');
                }}
              >
                <MenuItem value="INTEGRAL">Integral</MenuItem>
                <MenuItem value="PROPORCIONAL">Proporcional</MenuItem>
              </Select>
            </FormControl>

            {tipoCalculo === 'PROPORCIONAL' && (
              <Box>
                <FormControl fullWidth>
                  <InputLabel>Fatores de Atualização</InputLabel>
                  <Select
                    value={opcaoFator}
                    label="Fatores de Atualização"
                    onChange={(e) => setOpcaoFator(e.target.value)}
                  >
                    <MenuItem value="existente" disabled={!fatoresExistem}>
                      Usar fatores existentes no banco
                      {!fatoresExistem && ' (nenhum importado)'}
                    </MenuItem>
                    {podeImportarFatores && (
                      <MenuItem value="importar">Importar nova planilha</MenuItem>
                    )}
                  </Select>
                </FormControl>

                {!fatoresExistem && opcaoFator === 'existente' && (
                  <Alert severity="warning" sx={{ mt: 1 }}>
                    {podeImportarFatores
                      ? 'Nenhum fator de atualização encontrado. Importe uma planilha para continuar.'
                      : 'Nenhum fator de atualização está disponível. Solicite a importação a um diretor.'}
                  </Alert>
                )}

                {opcaoFator === 'importar' && (
                  <Box sx={{ mt: 2 }}>
                    <TextField
                      label="Portaria ou origem dos fatores"
                      fullWidth
                      sx={{ mb: 2 }}
                      value={portariaFator}
                      onChange={(e) => setPortariaFator(e.target.value)}
                    />
                    <Typography variant="body2" color="text.secondary" mb={1}>
                      Selecione a planilha de fatores (.xlsx):
                    </Typography>
                    <Button variant="outlined" component="label" fullWidth>
                      {arquivoFator ? arquivoFator.name : 'Selecionar arquivo .xlsx'}
                      <input
                        type="file"
                        accept=".xlsx"
                        hidden
                        onChange={(e) => setArquivoFator(e.target.files[0])}
                      />
                    </Button>
                  </Box>
                )}
              </Box>
            )}

          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogCalculo(false)}>Cancelar</Button>
          <Button
            variant="contained"
            disabled={calculando || importando ||
              (tipoCalculo === 'PROPORCIONAL' && opcaoFator === 'importar' && !arquivoFator) ||
              (tipoCalculo === 'PROPORCIONAL' && opcaoFator === 'importar' && !portariaFator.trim()) ||
              (tipoCalculo === 'PROPORCIONAL' && opcaoFator === 'existente' && !fatoresExistem)
            }
            onClick={async () => {
              // Se precisar importar primeiro
              if (tipoCalculo === 'PROPORCIONAL' && opcaoFator === 'importar' && arquivoFator) {
                setImportando(true);
                try {
                  await importarFatores(arquivoFator, portariaFator.trim());
                  setFatoresExistem(true);
                  setArquivoFator(null);
                  setPortariaFator('');
                  setOpcaoFator('existente');
                } catch (err) {
                  setErro(obterMensagemErro(err, 'Erro ao importar fatores de atualização'));
                  setImportando(false);
                  return;
                } finally {
                  setImportando(false);
                }
              }
              // Gera o cálculo
              await handleCalcular();
            }}
          >
            {importando ? <CircularProgress size={24} /> :
            calculando ? <CircularProgress size={24} /> :
            opcaoFator === 'importar' && arquivoFator ? 'Importar e Calcular' : 'Calcular'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* ABA 3: HISTRICO */}
      {aba === 3 && (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Data</TableCell>
                <TableCell>Status Anterior</TableCell>
                <TableCell>Status Atual</TableCell>
                <TableCell>Observação</TableCell>
                <TableCell>Usuário</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {historico.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} align="center">Nenhum histórico encontrado</TableCell>
                </TableRow>
              ) : historico.map((h) => (
                <TableRow key={h.id}>
                  <TableCell>{new Date(h.dtAlteracao).toLocaleString('pt-BR')}</TableCell>
                  <TableCell>
                    <Chip label={h.statusAnterior} size="small" color={STATUS_CORES[h.statusAnterior] || 'default'} />
                  </TableCell>
                  <TableCell>
                    <Chip label={h.statusAtual} size="small" color={STATUS_CORES[h.statusAtual] || 'default'} />
                  </TableCell>
                  <TableCell>{h.observacao}</TableCell>
                  <TableCell>{h.usuarioId}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {/* ABA 4: ASSISTENTE */}
      {aba === 4 && processo && (
        <AssistenteChat
          titulo="Assistente técnico do processo"
          altura="calc(100vh - 260px)"
          numeroProcesso={processo.numeroProcesso}
          mensagemInicial={`Olá! Estou olhando para o Processo Nº ${processo.numeroProcesso}. Posso ajudar a conferir documentos, memória de cálculo, CTS, ato e fundamentos legais aplicáveis.`}
        />
      )}
    </Layout>
  );
}

