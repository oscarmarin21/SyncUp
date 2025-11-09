export interface Song {
  id: number;
  titulo: string;
  artista: string;
  genero: string;
  año: number;
  duracion: number;
  audioUrl?: string;
}

export interface SearchRequest {
  artista?: string;
  genero?: string;
  año?: number;
  operador?: 'AND' | 'OR';
}

export interface RecommendationResponse {
  songs: Song[];
}

