import React from 'react';
import type { LucideIcon } from 'lucide-react';

interface StatCardProps {
  titulo: string;
  valor: string | number;
  subtitulo?: string;
  icono: LucideIcon;
  color?: 'indigo' | 'emerald' | 'amber' | 'blue' | 'purple';
}

export const StatCard: React.FC<StatCardProps> = ({
  titulo,
  valor,
  subtitulo,
  icono: Icon,
  color = 'indigo',
}) => {
  const colorMap = {
    indigo: 'bg-indigo-50 text-indigo-600 border-indigo-200',
    emerald: 'bg-emerald-50 text-emerald-600 border-emerald-200',
    amber: 'bg-amber-50 text-amber-600 border-amber-200',
    blue: 'bg-blue-50 text-blue-600 border-blue-200',
    purple: 'bg-purple-50 text-purple-600 border-purple-200',
  };

  return (
    <div className="bg-white rounded-xl p-6 border border-slate-200 shadow-sm flex items-start justify-between">
      <div>
        <p className="text-xs uppercase font-semibold text-slate-500 tracking-wider">
          {titulo}
        </p>
        <p className="text-2xl font-bold text-slate-900 mt-2">{valor}</p>
        {subtitulo && (
          <p className="text-xs text-slate-500 mt-1">{subtitulo}</p>
        )}
      </div>
      <div className={`p-3 rounded-xl border ${colorMap[color]}`}>
        <Icon className="w-6 h-6" />
      </div>
    </div>
  );
};
