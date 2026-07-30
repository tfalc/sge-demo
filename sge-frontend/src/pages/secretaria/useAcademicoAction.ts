import { useCallback, useState } from "react";

export function useAcademicoAction(reload: () => Promise<void>) {
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const runAction = useCallback(
    async (action: () => Promise<void>, okMessage: string) => {
      setSaving(true);
      setError(null);
      setSuccess(null);
      try {
        await action();
        setSuccess(okMessage);
        await reload();
      } catch {
        setError("Operacao falhou. Verifique dados duplicados ou campos obrigatorios.");
      } finally {
        setSaving(false);
      }
    },
    [reload],
  );

  return { saving, error, success, runAction };
}
