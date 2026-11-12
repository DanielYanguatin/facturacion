import { api } from './api';
import type { Cliente, CrearClientePayload } from '../types';

export const clienteService = {
  listar: async (busqueda?: string): Promise<Cliente[]> => {
    const res = await api.get<Cliente[]>('/clientes', {
      params: busqueda ? { busqueda } : {},
    });
    return res.data;
  },

  obtenerPorId: async (id: string): Promise<Cliente> => {
    const res = await api.get<Cliente>(`/clientes/${id}`);
    return res.data;
  },

  crear: async (payload: CrearClientePayload): Promise<Cliente> => {
    const res = await api.post<Cliente>('/clientes', payload);
    return res.data;
  },

  actualizar: async (id: string, payload: CrearClientePayload): Promise<Cliente> => {
    const res = await api.put<Cliente>(`/clientes/${id}`, payload);
    return res.data;
  },
};
