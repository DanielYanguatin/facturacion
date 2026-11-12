import React from 'react';
import { CheckCircle2, AlertTriangle, XCircle, Info } from 'lucide-react';

interface AlertBannerProps {
  tipo: 'exito' | 'error' | 'advertencia' | 'info';
  mensaje: string;
  detalles?: string[];
  onCerrar?: () => void;
}

export const AlertBanner: React.FC<AlertBannerProps> = ({
  tipo,
  mensaje,
  detalles,
  onCerrar,
}) => {
  const estilos = {
    exito: {
      bg: 'bg-emerald-50 border-emerald-300 text-emerald-800',
      icono: CheckCircle2,
      iconoColor: 'text-emerald-600',
    },
    error: {
      bg: 'bg-rose-50 border-rose-300 text-rose-800',
      icono: XCircle,
      iconoColor: 'text-rose-600',
    },
    advertencia: {
      bg: 'bg-amber-50 border-amber-300 text-amber-800',
      icono: AlertTriangle,
      iconoColor: 'text-amber-600',
    },
    info: {
      bg: 'bg-blue-50 border-blue-300 text-blue-800',
      icono: Info,
      iconoColor: 'text-blue-600',
    },
  }[tipo];

  const Icono = estilos.icono;

  return (
    <div className={`p-4 rounded-xl border ${estilos.bg} flex items-start gap-3 my-4 shadow-xs`}>
      <Icono className={`w-5 h-5 shrink-0 mt-0.5 ${estilos.iconoColor}`} />
      <div className="flex-1 text-sm">
        <p className="font-semibold">{mensaje}</p>
        {detalles && detalles.length > 0 && (
          <ul className="list-disc list-inside mt-1 space-y-0.5 text-xs opacity-90">
            {detalles.map((d, i) => (
              <li key={i}>{d}</li>
            ))}
          </ul>
        )}
      </div>
      {onCerrar && (
        <button
          onClick={onCerrar}
          className="text-xs font-semibold underline opacity-70 hover:opacity-100 cursor-pointer"
        >
          Cerrar
        </button>
      )}
    </div>
  );
};
