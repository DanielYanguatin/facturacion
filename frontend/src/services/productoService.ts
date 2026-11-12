import { api } from './api';
import type { ActualizarProductoPayload, CrearProductoPayload, Producto } from '../types';

export const productoService = {
  listar: async (params?: { nombre?: string; categoria?: string; soloActivos?: boolean }): Promise<Producto[]> => {
    const res = await api.get<Producto[]>('/productos', { params });
    return res.data;
  },

  obtenerPorId: async (id: string): Promise<Producto> => {
    const res = await api.get<Producto>(`/productos/${id}`);
    return res.data;
  },

  crear: async (payload: CrearProductoPayload): Promise<Producto> => {
    const res = await api.post<Producto>('/productos', payload);
    return res.data;
  },

  actualizar: async (id: string, payload: ActualizarProductoPayload): Promise<Producto> => {
    const res = await api.put<Producto>(`/productos/${id}`, payload);
    return res.data;
  },

  desactivar: async (id: string): Promise<Producto> => {
    const res = await api.delete<Producto>(`/productos/${id}`);
    return res.data;
  },

  subirImagen: async (id: string, archivo: File): Promise<Producto> => {
    const formData = new FormData();
    formData.append('imagen', archivo);
    const res = await api.post<Producto>(`/productos/${id}/imagen`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return res.data;
  },
};
