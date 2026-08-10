import { useEffect, useRef, useState } from 'react';
import {
  Avatar, Box, CircularProgress, Divider, IconButton, Paper, TextField, Typography
} from '@mui/material';
import SendIcon from '@mui/icons-material/Send';
import SmartToyIcon from '@mui/icons-material/SmartToy';
import PersonIcon from '@mui/icons-material/Person';
import { enviarMensagem } from '../../services/chatService';
import { obterMensagemErro } from '../../utils/apiError';

export default function AssistenteChat({
  titulo = 'Assistente Legislativo',
  altura = 420,
  numeroProcesso = null,
  mensagemInicial = 'Olá! Sou o assistente do GESPREV. Posso responder perguntas técnicas com base na legislação indexada e nos dados do sistema.'
}) {
  const [mensagens, setMensagens] = useState([{ tipo: 'bot', texto: mensagemInicial }]);
  const [input, setInput] = useState('');
  const [carregando, setCarregando] = useState(false);
  const fimRef = useRef(null);

  useEffect(() => {
    fimRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [mensagens]);

  const handleEnviar = async () => {
    if (!input.trim() || carregando) return;
    const pergunta = input.trim();
    setInput('');
    setMensagens(prev => [...prev, { tipo: 'usuario', texto: pergunta }]);
    setCarregando(true);

    try {
      const resposta = await enviarMensagem(pergunta, numeroProcesso);
      setMensagens(prev => [...prev, { tipo: 'bot', texto: resposta }]);
    } catch (err) {
      setMensagens(prev => [...prev, {
        tipo: 'bot',
        texto: obterMensagemErro(
          err,
          'O assistente está indisponível no momento. Verifique a conexão com o servidor de IA/RAG e tente novamente.'
        )
      }]);
    } finally {
      setCarregando(false);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleEnviar();
    }
  };

  return (
    <Paper elevation={2} sx={{ height: altura, display: 'flex', flexDirection: 'column' }}>
      <Box sx={{ p: 2 }}>
        <Typography variant="h6" fontWeight="bold">{titulo}</Typography>
        {numeroProcesso && (
          <Typography variant="body2" color="text.secondary">
            Contexto fixo: Processo Nº {numeroProcesso}
          </Typography>
        )}
      </Box>
      <Divider />

      <Box sx={{ flexGrow: 1, overflowY: 'auto', p: 2, display: 'flex', flexDirection: 'column', gap: 2 }}>
        {mensagens.map((msg, index) => (
          <Box
            key={index}
            sx={{
              display: 'flex',
              justifyContent: msg.tipo === 'usuario' ? 'flex-end' : 'flex-start',
              gap: 1,
              alignItems: 'flex-start'
            }}
          >
            {msg.tipo === 'bot' && (
              <Avatar sx={{ bgcolor: 'primary.main', width: 30, height: 30 }}>
                <SmartToyIcon fontSize="small" />
              </Avatar>
            )}
            <Paper
              elevation={1}
              sx={{
                p: 1.5,
                maxWidth: '78%',
                bgcolor: msg.tipo === 'usuario' ? 'primary.main' : 'grey.100',
                color: msg.tipo === 'usuario' ? 'white' : 'text.primary',
                borderRadius: msg.tipo === 'usuario' ? '16px 16px 4px 16px' : '16px 16px 16px 4px'
              }}
            >
              <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>
                {msg.texto}
              </Typography>
            </Paper>
            {msg.tipo === 'usuario' && (
              <Avatar sx={{ bgcolor: 'secondary.main', width: 30, height: 30 }}>
                <PersonIcon fontSize="small" />
              </Avatar>
            )}
          </Box>
        ))}

        {carregando && (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Avatar sx={{ bgcolor: 'primary.main', width: 30, height: 30 }}>
              <SmartToyIcon fontSize="small" />
            </Avatar>
            <Paper elevation={1} sx={{ p: 1.5, bgcolor: 'grey.100', borderRadius: '16px 16px 16px 4px' }}>
              <CircularProgress size={16} />
            </Paper>
          </Box>
        )}
        <div ref={fimRef} />
      </Box>

      <Divider />
      <Box sx={{ p: 1.5, display: 'flex', gap: 1, alignItems: 'flex-end' }}>
        <TextField
          fullWidth
          multiline
          maxRows={4}
          placeholder={numeroProcesso ? 'Pergunte sobre este processo e a legislação aplicável...' : 'Pergunte sobre legislação, processos ou estatísticas...'}
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={handleKeyDown}
          disabled={carregando}
          size="small"
        />
        <IconButton color="primary" onClick={handleEnviar} disabled={!input.trim() || carregando} sx={{ mb: 0.5 }}>
          <SendIcon />
        </IconButton>
      </Box>
    </Paper>
  );
}
