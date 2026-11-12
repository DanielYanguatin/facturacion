import React from 'react';
import { X } from 'lucide-react';

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  titulo: string;
  children: React.ReactNode;
  ancho?: 'normal' | 'ancho' | 'completo';
}

export const Modal: React.FC<ModalProps> = ({
  isOpen,
  onClose,
  titulo,
  children,
  ancho = 'normal',
}) => {
  if (!isOpen) return null;

  const anchoClases = {
    normal: 'max-w-lg',
    ancho: 'max-w-2xl',
    completo: 'max-w-4xl',
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4">
      <div className={`bg-white rounded-2xl shadow-2xl w-full ${anchoClases[ancho]} overflow-hidden animate-in fade-in zoom-in-95 duration-150`}>
        <div className="px-6 py-4 border-b border-slate-200 flex items-center justify-between bg-slate-50">
          <h3 className="font-bold text-lg text-slate-800">{titulo}</h3>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-200/60 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>
        <div className="p-6 max-h-[80vh] overflow-y-auto">{children}</div>
      </div>
    </div>
  );
};
