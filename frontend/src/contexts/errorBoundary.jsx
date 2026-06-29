import { Component } from 'react'
import ErrorFrontend from '@/views/public/errores/error-frontend/error-frontend.jsx'

class ErrorBoundary extends Component {
  constructor(props) {
    super(props)
    this.state = { tieneError: false }
  }

  static getDerivedStateFromError() {
    return { tieneError: true }
  }

  componentDidCatch(error, info) {
    console.error('Error de renderizado:', error, info)
  }

  render() {
    if (this.state.tieneError) {
      return <ErrorFrontend />
    }
    return this.props.children
  }
}

export default ErrorBoundary