const API = "http://localhost:8080/transacoes";

const usuario = JSON.parse(localStorage.getItem("usuario"));
const token = localStorage.getItem("token");

function sair() {
    localStorage.removeItem("token");
    localStorage.removeItem("usuario");
    window.location.href = "login.html";
}


if (!usuario || !token) {
    sair();
}


async function apiFetch(url, opcoes = {}) {
    const res = await fetch(url, {
        ...opcoes,
        headers: { ...(opcoes.headers || {}), "Authorization": "Bearer " + token }
    });
    if (res.status === 401) {
        sair();
    }
    return res;
}

const Imes = document.querySelector("#mes");
const Idata = document.querySelector("#data");
const form = document.querySelector("#formTransacao");

let editandoId = null; // null = criando; número = editando essa transação

const brl = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });

function hojeISO() {
    const d = new Date();
    const mm = String(d.getMonth() + 1).padStart(2, "0");
    const dd = String(d.getDate()).padStart(2, "0");
    return `${d.getFullYear()}-${mm}-${dd}`;
}

function formatarData(iso) {
    const [a, m, d] = iso.split("-");
    return `${d}/${m}/${a}`;
}

function periodoAtual() {
    // Se o campo de mês estiver vazio ou inválido, volta para o mês atual
    if (!/^\d{4}-\d{2}$/.test(Imes.value)) {
        Imes.value = hojeISO().slice(0, 7);
    }
    const [ano, mes] = Imes.value.split("-").map(Number);
    return { ano, mes };
}

async function carregar() {
    const { ano, mes } = periodoAtual();
    const params = `mes=${mes}&ano=${ano}`;

    try {
        const [resLista, resResumo] = await Promise.all([
            apiFetch(`${API}?${params}`),
            apiFetch(`${API}/resumo?${params}`)
        ]);

        if (!resLista.ok || !resResumo.ok) {
            alert(`Erro ao carregar os dados (lista: ${resLista.status}, resumo: ${resResumo.status}).`);
            return;
        }

        mostrarResumo(await resResumo.json());
        mostrarLista(await resLista.json());
    } catch (e) {
        console.error(e);
        alert("Não foi possível conectar à API.");
    }
}

function mostrarResumo(r) {
    document.querySelector("#receitas").textContent = brl.format(r.receitas);
    document.querySelector("#despesas").textContent = brl.format(r.despesas);

    const saldo = document.querySelector("#saldo");
    saldo.textContent = brl.format(r.saldo);
    saldo.className = r.saldo < 0 ? "vermelho" : "verde";

    const box = document.querySelector("#categoriasLista");
    box.innerHTML = "";

    const itens = Object.entries(r.despesasPorCategoria).sort((a, b) => b[1] - a[1]);
    if (itens.length === 0) {
        box.innerHTML = '<p class="vazio">Nenhuma despesa neste mês.</p>';
        return;
    }

    // Base do cálculo: a receita do mês. Sem receita, usa o total de despesas.
    const semReceita = !(r.receitas > 0);
    const base = semReceita ? r.despesas : r.receitas;
    const pct = new Intl.NumberFormat("pt-BR", { maximumFractionDigits: 1 });

    if (semReceita) {
        const aviso = document.createElement("p");
        aviso.className = "vazio";
        aviso.textContent = "Sem receita neste mês: porcentagens sobre o total de despesas.";
        box.append(aviso);
    }

    itens.forEach(([categoria, valor]) => {
        const percentual = base > 0 ? (valor / base * 100) : 0;

        const item = document.createElement("div");
        const topo = document.createElement("div");
        topo.className = "barra-topo";

        const nome = document.createElement("span");
        nome.textContent = categoria;
        const total = document.createElement("span");
        total.textContent = `${brl.format(valor)} · ${pct.format(percentual)}%`;
        topo.append(nome, total);

        const barra = document.createElement("div");
        barra.className = "barra";
        const preenchida = document.createElement("div");
        preenchida.style.width = Math.min(percentual, 100) + "%";
        if (percentual > 100) preenchida.classList.add("estourou");
        barra.append(preenchida);

        item.append(topo, barra);
        box.append(item);
    });
}

function mostrarLista(transacoes) {
    const ul = document.querySelector("#lista");
    ul.innerHTML = "";

    if (transacoes.length === 0) {
        ul.innerHTML = '<li class="vazio">Nenhuma transação neste mês.</li>';
        return;
    }

    transacoes.forEach(t => {
        const li = document.createElement("li");

        const info = document.createElement("div");
        info.className = "info";
        const titulo = document.createElement("b");
        titulo.textContent = t.descricao;
        const detalhe = document.createElement("small");
        detalhe.textContent = `${t.categoria} · ${formatarData(t.data)}`;
        info.append(titulo, detalhe);

        const direita = document.createElement("div");
        direita.className = "direita";

        const valor = document.createElement("span");
        const receita = t.tipo === "RECEITA";
        valor.className = "valor " + (receita ? "verde" : "vermelho");
        valor.textContent = (receita ? "+ " : "- ") + brl.format(t.valor);

        const editar = document.createElement("button");
        editar.type = "button";
        editar.className = "editar";
        editar.title = "Editar";
        editar.textContent = "✎";
        editar.addEventListener("click", () => iniciarEdicao(t));

        const excluir = document.createElement("button");
        excluir.type = "button";
        excluir.className = "excluir";
        excluir.title = "Excluir";
        excluir.textContent = "✕";
        excluir.addEventListener("click", () => excluirTransacao(t.id));

        direita.append(valor, editar, excluir);
        li.append(info, direita);
        ul.append(li);
    });
}

function iniciarEdicao(t) {
    editandoId = t.id;
    document.querySelector("#descricao").value = t.descricao;
    document.querySelector("#valor").value = t.valor;
    document.querySelector("#tipo").value = t.tipo;
    document.querySelector("#categoria").value = t.categoria;
    Idata.value = t.data;

    document.querySelector("#tituloForm").textContent = "Editar transação";
    document.querySelector("#btnSalvar").textContent = "Salvar alterações";
    document.querySelector("#btnCancelar").hidden = false;
    form.scrollIntoView({ behavior: "smooth", block: "center" });
}

function cancelarEdicao() {
    editandoId = null;
    form.reset();
    Idata.value = hojeISO();
    document.querySelector("#tituloForm").textContent = "Nova transação";
    document.querySelector("#btnSalvar").textContent = "Adicionar";
    document.querySelector("#btnCancelar").hidden = true;
}

async function excluirTransacao(id) {
    if (!confirm("Excluir esta transação?")) return;
    try {
        const res = await apiFetch(`${API}/${id}`, { method: "DELETE" });
        if (!res.ok) {
            alert("Não foi possível excluir.");
            return;
        }
        if (editandoId === id) cancelarEdicao();
        carregar();
    } catch (e) {
        alert("Não foi possível conectar à API.");
    }
}

form.addEventListener("submit", async function (event) {
    event.preventDefault();

    const corpo = {
        descricao: document.querySelector("#descricao").value,
        valor: parseFloat(document.querySelector("#valor").value),
        tipo: document.querySelector("#tipo").value,
        categoria: document.querySelector("#categoria").value,
        data: Idata.value
    };

    try {
        const url = editandoId ? `${API}/${editandoId}` : API;
        const res = await apiFetch(url, {
            method: editandoId ? "PUT" : "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(corpo)
        });

        if (res.status === 400) {
            const erros = await res.json();
            alert(Object.values(erros).join("\n"));
            return;
        }
        if (!res.ok) {
            alert(res.status === 404 ? "Transação não encontrada." : "Não foi possível salvar a transação.");
            return;
        }

        cancelarEdicao();
        // Mostra o mês da transação recém-criada
        Imes.value = corpo.data.slice(0, 7);
        carregar();
    } catch (e) {
        alert("Não foi possível conectar à API.");
    }
});

document.querySelector("#sair").addEventListener("click", sair);
document.querySelector("#btnCancelar").addEventListener("click", cancelarEdicao);

Imes.addEventListener("change", carregar);

// Início
document.querySelector("#boasVindas").textContent = "Olá, " + usuario.nome + "!";
Idata.value = hojeISO();
Imes.value = hojeISO().slice(0, 7);
carregar();