import { api } from './api';
import type { DashboardResumen } from '../types';

export const dashboardService = {
  obtenerResumen: async (): Promise<DashboardResumen> => {
    const res = await api.get<DashboardResumen>('/dashboard/resumen');
    return res.data;
  },
};
