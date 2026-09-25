import { useState } from 'react'
import { ApiError, messaggioErrore } from '../api/client'

/**
 * Stato comune dei form collegati alle API: invio in corso, errore generale (detail del ProblemDetail)
 * ed errori per campo (ProblemDetail.errors, restituito dal backend sui 400 di validazione).
 */
export function useApiForm() {
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})

  async function run<T>(action: () => Promise<T>): Promise<T | undefined> {
    setSubmitting(true)
    setError(null)
    setFieldErrors({})
    try {
      return await action()
    } catch (err) {
      if (err instanceof ApiError && Object.keys(err.fieldErrors).length > 0) {
        setFieldErrors(err.fieldErrors)
      }
      setError(messaggioErrore(err))
      throw err
    } finally {
      setSubmitting(false)
    }
  }

  /** Come run, ma senza propagare l'errore (già mostrato tramite error/fieldErrors). */
  async function submit<T>(action: () => Promise<T>) {
    try {
      return await run(action)
    } catch {
      return undefined
    }
  }

  return { submitting, error, setError, fieldErrors, setFieldErrors, run, submit }
}
