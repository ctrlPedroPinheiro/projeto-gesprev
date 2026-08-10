import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../contexts/useAuth';
import {
  Box, Drawer, AppBar, Toolbar, Typography, List, ListItem,
  ListItemButton, ListItemIcon, ListItemText, IconButton, Avatar,
  Menu, MenuItem, Divider, useMediaQuery, useTheme
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import DashboardIcon from '@mui/icons-material/Dashboard';
import FolderIcon from '@mui/icons-material/Folder';
import PersonIcon from '@mui/icons-material/Person';
import LogoutIcon from '@mui/icons-material/Logout';
import ChatIcon from '@mui/icons-material/Chat';

const DRAWER_WIDTH = 240;

const menuItems = [
  { texto: 'Dashboard', icone: <DashboardIcon />, rota: '/dashboard' },
  { texto: 'Processos', icone: <FolderIcon />, rota: '/processos' },
  { texto: 'Assistente', icone: <ChatIcon />, rota: '/chat' },
];

const menuDiretor = [
  { texto: 'Usuários', icone: <PersonIcon />, rota: '/usuarios' },
];

export default function Layout({ children }) {
  const { usuario, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const [anchorEl, setAnchorEl] = useState(null);
  const [mobileOpen, setMobileOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const navegar = (rota) => {
    navigate(rota);
    setMobileOpen(false);
  };

  return (
    <Box sx={{ display: 'flex' }}>
      {/* AppBar */}
      <AppBar
        position="fixed"
        sx={{
          zIndex: (currentTheme) => currentTheme.zIndex.drawer + 1,
          width: { md: `calc(100% - ${DRAWER_WIDTH}px)` },
          ml: { md: `${DRAWER_WIDTH}px` }
        }}
      >
        <Toolbar sx={{ justifyContent: 'space-between' }}>
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <IconButton
              color="inherit"
              aria-label="Abrir menu de navegação"
              edge="start"
              onClick={() => setMobileOpen(true)}
              sx={{ mr: 1, display: { md: 'none' } }}
            >
              <MenuIcon />
            </IconButton>
            <Typography variant="h6" fontWeight="bold">GESPREV</Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography variant="body2" sx={{ display: { xs: 'none', sm: 'block' } }}>
              {usuario?.nome}
            </Typography>
            <IconButton
              aria-label="Abrir menu do usuário"
              onClick={(e) => setAnchorEl(e.currentTarget)}
            >
              <Avatar sx={{ width: 32, height: 32, bgcolor: 'white', color: 'primary.main' }}>
                {usuario?.nome?.charAt(0)}
              </Avatar>
            </IconButton>
          </Box>
        </Toolbar>
      </AppBar>

      {/* Menu do avatar */}
      <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={() => setAnchorEl(null)}>
        <MenuItem disabled>
          <Typography variant="body2" color="text.secondary">{usuario?.perfil}</Typography>
        </MenuItem>
        <Divider />
        <MenuItem onClick={handleLogout}>
          <ListItemIcon><LogoutIcon fontSize="small" /></ListItemIcon>
          Sair
        </MenuItem>
      </Menu>

      {/* Drawer lateral */}
      <Drawer
        variant={isMobile ? 'temporary' : 'permanent'}
        open={isMobile ? mobileOpen : true}
        onClose={() => setMobileOpen(false)}
        ModalProps={{ keepMounted: true }}
        sx={{
          width: { md: DRAWER_WIDTH },
          flexShrink: { md: 0 },
          '& .MuiDrawer-paper': { width: DRAWER_WIDTH, boxSizing: 'border-box' }
        }}
      >
        <Toolbar />
        <Box sx={{ overflow: 'auto', mt: 1 }}>
          <List>
            {menuItems.map((item) => (
              <ListItem key={item.texto} disablePadding>
                <ListItemButton
                  selected={location.pathname === item.rota}
                  onClick={() => navegar(item.rota)}
                >
                  <ListItemIcon>{item.icone}</ListItemIcon>
                  <ListItemText primary={item.texto} />
                </ListItemButton>
              </ListItem>
            ))}
          </List>

          {usuario?.perfil === 'DIRETOR' && (
            <>
              <Divider sx={{ my: 1 }} />
              <Typography variant="caption" sx={{ px: 2, color: 'text.secondary' }}>
                ADMINISTRAÇÃO
              </Typography>
              <List>
                {menuDiretor.map((item) => (
                  <ListItem key={item.texto} disablePadding>
                    <ListItemButton
                      selected={location.pathname === item.rota}
                      onClick={() => navegar(item.rota)}
                    >
                      <ListItemIcon>{item.icone}</ListItemIcon>
                      <ListItemText primary={item.texto} />
                    </ListItemButton>
                  </ListItem>
                ))}
              </List>
            </>
          )}
        </Box>
      </Drawer>

      {/* Conteúdo principal */}
      <Box
        component="main"
        sx={{ flexGrow: 1, minWidth: 0, width: '100%', p: { xs: 2, sm: 3 }, mt: 8 }}
      >
        {children}
      </Box>
    </Box>
  );
}
