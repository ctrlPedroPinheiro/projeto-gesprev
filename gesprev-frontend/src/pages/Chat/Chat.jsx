import Layout from '../../components/Layout/Layout';
import AssistenteChat from '../../components/AssistenteChat/AssistenteChat';

export default function Chat() {
  return (
    <Layout>
      <AssistenteChat
        altura="calc(100vh - 160px)"
        titulo="Assistente Legislativo"
        mensagemInicial="Olá! Sou o assistente do GESPREV. Posso responder perguntas sobre legislação previdenciária, regras de aposentadoria e processos do sistema."
      />
    </Layout>
  );
}
