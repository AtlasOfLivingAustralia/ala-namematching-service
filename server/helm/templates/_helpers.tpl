{{/*
Expand the name of the chart.
*/}}
{{- define "ala-namematching.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
*/}}
{{- define "ala-namematching.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "ala-namematching.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "ala-namematching.labels" -}}
helm.sh/chart: {{ include "ala-namematching.chart" . }}
{{ include "ala-namematching.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "ala-namematching.selectorLabels" -}}
app.kubernetes.io/name: {{ include "ala-namematching.fullname" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Deployment annotations — checksum forces a pod roll when the ConfigMap changes.
*/}}
{{- define "ala-namematching.deploymentAnnotations" -}}
{{- $checksumInput := dict "config" .Values.config "groups.json" (.Files.Get "config/groups.json") "subgroups.json" (.Files.Get "config/subgroups.json") -}}
{{- $configChecksum := dict "checksum/config" ($checksumInput | toJson | sha256sum) -}}
{{- $annotations := merge (default (dict) .Values.deploymentAnnotations) $configChecksum -}}
{{ toYaml $annotations }}
{{- end }}
