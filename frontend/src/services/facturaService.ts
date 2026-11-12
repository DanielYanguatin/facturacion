import { api } from './api';
import type { CrearFacturaPayload, Factura } from '../types';

export const facturaService = {
  listar: async (params?: {
    numeroFactura?: string;
    idCliente?: string;
    fechaDesde?: string;
    fechaHasta?: string;
  }): Promise<Factura[]> => {
    const res = await api.get<Factura[]>('/facturas', { params });
    return res.data;
  },

  obtenerPorId: async (id: number): Promise<Factura> => {
    const res = await api.get<Factura>(`/facturas/${id}`);
    return res.data;
  },

  obtenerPorNumero: async (numero: string): Promise<Factura> => {
    const res = await api.get<Factura>(`/facturas/numero/${numero}`);
    return res.data;
  },

  obtenerPorCliente: async (clienteId: string): Promise<Factura[]> => {
    const res = await api.get<Factura[]>(`/facturas/cliente/${clienteId}`);
    return res.data;
  },

  crear: async (payload: CrearFacturaPayload): Promise<Factura> => {
    const res = await api.post<Factura>('/facturas', payload);
    return res.data;
  },
};
